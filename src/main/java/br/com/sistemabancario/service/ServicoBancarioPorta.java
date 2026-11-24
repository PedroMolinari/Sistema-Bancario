package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/** Casos de uso bancários consumidos pela camada de apresentação. */
public interface ServicoBancarioPorta {
    List<Conta> listarContas();

    Conta obterConta(UUID id);

    Conta criarConta(Cliente cliente, BigDecimal saldoInicial);

    void excluirConta(UUID id);

    Transacao depositar(UUID contaId, BigDecimal valor);

    Transacao sacar(UUID contaId, BigDecimal valor);

    Transacao transferir(UUID origemId, UUID destinoId, BigDecimal valor);

    default Transacao transferirPorNumeroPublico(UUID origemId, long destinoNumero, BigDecimal valor) {
        throw new UnsupportedOperationException("A transferência por número público não está disponível.");
    }

    List<Transacao> consultarExtrato(UUID contaId);

    List<Transacao> consultarMovimentacoesRecentes(int limite);

    Path exportarExtrato(UUID contaId, Path arquivo);
}
