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

    Transacao depositar(UUID contaId, BigDecimal valor);

    Transacao sacar(UUID contaId, BigDecimal valor);

    Transacao transferir(UUID origemId, UUID destinoId, BigDecimal valor);

    List<Transacao> consultarExtrato(UUID contaId);

    Path exportarExtrato(UUID contaId, Path arquivo);
}
