package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Consultas bancárias minimizadas e autorizadas para uso pelo assistente. */
public final class AssistenteBancarioService {
    private final ConsultaBancariaPorta consultas;

    public AssistenteBancarioService(ConsultaBancariaPorta consultas) {
        this.consultas = Objects.requireNonNull(consultas, "A porta de consulta bancária é obrigatória.");
    }

    public List<ContaResumo> listarContas() {
        return consultas.listarContas().stream()
                .map(conta -> new ContaResumo(conta.getIdentificadorCurto(), conta.getSaldo()))
                .toList();
    }

    public BigDecimal consultarSaldo(String identificadorCurto) {
        Conta conta = consultarConta(identificadorCurto);
        return conta.getSaldo();
    }

    public List<MovimentacaoResumo> consultarExtrato(String identificadorCurto) {
        UUID contaId = decodificarIdentificador(identificadorCurto);
        consultas.obterConta(contaId);
        return consultas.consultarExtrato(contaId).stream()
                .map(AssistenteBancarioService::resumir)
                .toList();
    }

    private Conta consultarConta(String identificadorCurto) {
        return consultas.obterConta(decodificarIdentificador(identificadorCurto));
    }

    private static UUID decodificarIdentificador(String identificadorCurto) {
        Objects.requireNonNull(identificadorCurto, "O identificador da conta é obrigatório.");
        try {
            BigInteger valor = new BigInteger(identificadorCurto, 36);
            if (valor.signum() < 0 || valor.bitLength() > 128) throw new NumberFormatException();
            byte[] bytes = new byte[16];
            byte[] magnitude = valor.toByteArray();
            int origem = magnitude.length > 16 ? magnitude.length - 16 : 0;
            int tamanho = magnitude.length - origem;
            System.arraycopy(magnitude, origem, bytes, 16 - tamanho, tamanho);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return new UUID(buffer.getLong(), buffer.getLong());
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("O identificador curto da conta é inválido.", erro);
        }
    }

    private static MovimentacaoResumo resumir(Transacao transacao) {
        return new MovimentacaoResumo(transacao.tipo(), transacao.valor(), transacao.dataHora(),
                Conta.formatarIdentificador(transacao.contaOrigem()),
                transacao.contaDestino() == null ? null : Conta.formatarIdentificador(transacao.contaDestino()));
    }

    /** Identificador curto e saldo; não contém nome ou UUID completo. */
    public record ContaResumo(String identificador, BigDecimal saldo) { }

    /** Dados necessários para descrever uma movimentação sem IDs internos completos. */
    public record MovimentacaoResumo(Transacao.Tipo tipo, BigDecimal valor, LocalDateTime dataHora,
                                     String contaOrigem, String contaDestino) { }
}
