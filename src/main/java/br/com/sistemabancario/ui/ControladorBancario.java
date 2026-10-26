package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import br.com.sistemabancario.service.ServicoBancarioPorta;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Encaminha as ações da interface aos casos de uso, sem conter regras bancárias ou Swing. */
public final class ControladorBancario {
    private final ServicoBancarioPorta servico;

    public ControladorBancario(ServicoBancarioPorta servico) {
        this.servico = Objects.requireNonNull(servico, "O serviço bancário é obrigatório.");
    }

    public List<Conta> listarContas() {
        return servico.listarContas();
    }

    public Conta obterConta(UUID id) {
        return servico.obterConta(id);
    }

    public Conta criarConta(Cliente cliente, BigDecimal saldoInicial) {
        return servico.criarConta(cliente, saldoInicial);
    }

    public void excluirConta(UUID id) { servico.excluirConta(id); }

    public Transacao depositar(UUID contaId, BigDecimal valor) {
        return servico.depositar(contaId, valor);
    }

    public Transacao sacar(UUID contaId, BigDecimal valor) {
        return servico.sacar(contaId, valor);
    }

    public Transacao transferir(UUID origemId, UUID destinoId, BigDecimal valor) {
        return servico.transferir(origemId, destinoId, valor);
    }

    public List<Transacao> consultarExtrato(UUID contaId) {
        return servico.consultarExtrato(contaId);
    }

    public Path exportarExtrato(UUID contaId, Path arquivo) {
        return servico.exportarExtrato(contaId, arquivo);
    }
}
