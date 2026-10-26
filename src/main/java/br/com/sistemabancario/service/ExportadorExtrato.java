package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Transacao;
import br.com.sistemabancario.domain.Conta;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ExportadorExtrato {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final ServicoBancarioPorta banco;

    public ExportadorExtrato(ServicoBancarioPorta banco) { this.banco = banco; }

    public Path exportar(UUID contaId, Path arquivo) {
        var conta = banco.obterConta(contaId);
        var transacoes = banco.consultarExtrato(contaId);
        StringBuilder texto = new StringBuilder()
                .append("EXTRATO BANCÁRIO\n")
                .append("Conta: ").append(conta.getIdentificadorCurto()).append('\n')
                .append("Titular: ").append(conta.getTitular().getNome()).append('\n')
                .append("Saldo atual: R$ ").append(conta.getSaldo().toPlainString()).append("\n\n");

        if (transacoes.isEmpty()) texto.append("Nenhuma transação registrada.\n");
        for (Transacao transacao : transacoes) {
            boolean credito = transacao.contaDestino() != null
                    ? transacao.contaDestino().equals(contaId)
                    : transacao.tipo() == Transacao.Tipo.DEPOSITO;
            BigDecimal valor = transacao.valor();
            texto.append(transacao.dataHora().format(FORMATO_DATA)).append(" | ")
                    .append(transacao.tipo()).append(" | ")
                    .append(credito ? "Crédito" : "Débito").append(" R$ ").append(valor.toPlainString())
                    .append(" | Origem: ").append(Conta.formatarIdentificador(transacao.contaOrigem()))
                    .append(" | Destino: ").append(transacao.contaDestino() == null ? "—"
                            : Conta.formatarIdentificador(transacao.contaDestino()))
                    .append(" | Resultado: CONCLUÍDA\n");
        }

        try {
            Path destino = arquivo.toAbsolutePath().normalize();
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());
            Files.writeString(destino, texto.toString(), StandardCharsets.UTF_8);
            return destino;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível exportar o extrato.", e);
        }
    }
}
