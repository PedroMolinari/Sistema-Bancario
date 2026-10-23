package br.com.sistemabancario.repository.jdbc;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import br.com.sistemabancario.exception.PersistenciaException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Coordena gravações que precisam alterar saldo e histórico no mesmo commit. */
public final class BancoRepository {
    private final ConexaoBanco banco;
    private final ClienteRepository clientes;
    private final ContaRepository contas;
    private final TransacaoRepository transacoes;

    public BancoRepository(ConexaoBanco banco) {
        this.banco = banco;
        clientes = new ClienteRepository(banco);
        contas = new ContaRepository(banco);
        transacoes = new TransacaoRepository(banco);
        banco.inicializar();
    }

    public ConexaoBanco getConexaoBanco() { return banco; }
    public void cadastrarCliente(Cliente cliente) { clientes.salvar(cliente); }

    public void criarConta(Conta conta) {
        banco.emTransacao(conexao -> {
            clientes.salvar(conexao, conta.getTitular());
            contas.salvar(conexao, conta);
            return null;
        });
    }

    public Optional<Conta> buscarConta(UUID id) { return contas.buscar(id); }
    public List<Conta> listarContas() { return contas.listar(); }
    public List<Transacao> listarTransacoes(UUID contaId) { return transacoes.listarPorConta(contaId); }

    public void depositar(UUID contaId, java.math.BigDecimal anterior,
                          java.math.BigDecimal novo, Transacao transacao) {
        gravarMovimento(conexao -> contas.atualizarSaldo(conexao, contaId, anterior, novo), transacao);
    }

    public void sacar(UUID contaId, java.math.BigDecimal anterior,
                      java.math.BigDecimal novo, Transacao transacao) {
        gravarMovimento(conexao -> contas.atualizarSaldo(conexao, contaId, anterior, novo), transacao);
    }

    public void transferir(UUID origemId, java.math.BigDecimal saldoOrigem,
                           java.math.BigDecimal novoSaldoOrigem, UUID destinoId,
                           java.math.BigDecimal saldoDestino, java.math.BigDecimal novoSaldoDestino,
                           Transacao transacao) {
        banco.emTransacao(conexao -> {
            contas.atualizarSaldo(conexao, origemId, saldoOrigem, novoSaldoOrigem);
            contas.atualizarSaldo(conexao, destinoId, saldoDestino, novoSaldoDestino);
            transacoes.salvar(conexao, transacao);
            return null;
        });
    }

    private void gravarMovimento(AtualizacaoSaldo atualizacao, Transacao transacao) {
        banco.emTransacao(conexao -> {
            atualizacao.executar(conexao);
            transacoes.salvar(conexao, transacao);
            return null;
        });
    }

    @FunctionalInterface
    private interface AtualizacaoSaldo { void executar(Connection conexao) throws SQLException; }
}
