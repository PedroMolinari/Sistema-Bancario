package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import br.com.sistemabancario.repository.jdbc.BancoRepository;
import br.com.sistemabancario.repository.jdbc.ConexaoBanco;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Regras de aplicação; persiste saldo e transação em um único commit antes de publicar a mudança em memória. */
public final class ServicoBancario implements ServicoBancarioPorta {
    private final BancoRepository repositorio;
    private final ConcurrentHashMap<UUID, Conta> contas = new ConcurrentHashMap<>();

    public ServicoBancario() { this(new ConexaoBanco()); }

    public ServicoBancario(Path arquivoBanco) { this(new ConexaoBanco(arquivoBanco)); }

    public ServicoBancario(ConexaoBanco conexaoBanco) {
        repositorio = new BancoRepository(conexaoBanco);
        for (Conta conta : repositorio.listarContas()) contas.put(conta.getIdentificador(), conta);
    }

    public void cadastrarCliente(Cliente cliente) { repositorio.cadastrarCliente(cliente); }

    public Conta criarConta(Cliente cliente, BigDecimal saldoInicial) {
        Conta conta = new Conta(cliente, saldoInicial);
        repositorio.criarConta(conta);
        contas.put(conta.getIdentificador(), conta);
        return conta;
    }

    @Override
    public void excluirConta(UUID id) {
        Conta conta = obterConta(id);
        synchronized (conta) {
            if (repositorio.contaPossuiMovimentacoes(id)) {
                throw new IllegalStateException(
                        "Esta conta não pode ser excluída porque possui movimentações registradas.");
            }
            repositorio.excluirConta(id);
            contas.remove(id, conta);
        }
    }

    public Conta obterConta(UUID id) {
        Conta existente = contas.get(id);
        if (existente != null) return existente;
        Conta carregada = repositorio.buscarConta(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + id));
        Conta concorrente = contas.putIfAbsent(id, carregada);
        return concorrente == null ? carregada : concorrente;
    }

    public List<Conta> listarContas() {
        return repositorio.listarContas().stream().map(contaCarregada -> {
            Conta existente = contas.putIfAbsent(contaCarregada.getIdentificador(), contaCarregada);
            return existente == null ? contaCarregada : existente;
        }).toList();
    }

    public Transacao depositar(UUID contaId, BigDecimal valor) {
        Conta conta = obterConta(contaId);
        synchronized (conta) {
            Conta.validarValorPositivo(valor);
            BigDecimal anterior = conta.getSaldo();
            BigDecimal novo = anterior.add(valor);
            Transacao transacao = criarTransacao(Transacao.Tipo.DEPOSITO, valor, contaId, null);
            repositorio.depositar(contaId, anterior, novo, transacao);
            conta.depositar(valor);
            return transacao;
        }
    }

    public Transacao sacar(UUID contaId, BigDecimal valor) {
        Conta conta = obterConta(contaId);
        synchronized (conta) {
            Conta.validarValorPositivo(valor);
            BigDecimal anterior = conta.getSaldo();
            if (anterior.compareTo(valor) < 0) {
                throw new IllegalStateException("Saldo insuficiente para realizar o saque.");
            }
            BigDecimal novo = anterior.subtract(valor);
            Transacao transacao = criarTransacao(Transacao.Tipo.SAQUE, valor, contaId, null);
            repositorio.sacar(contaId, anterior, novo, transacao);
            conta.sacar(valor);
            return transacao;
        }
    }

    public Transacao transferir(UUID origemId, UUID destinoId, BigDecimal valor) {
        Conta origem = obterConta(origemId);
        Conta destino = obterConta(destinoId);
        if (origemId.equals(destinoId)) throw new IllegalArgumentException("As contas devem ser diferentes.");

        Conta primeira = origemId.compareTo(destinoId) < 0 ? origem : destino;
        Conta segunda = primeira == origem ? destino : origem;
        synchronized (primeira) {
            synchronized (segunda) {
                Conta.validarValorPositivo(valor);
                BigDecimal saldoOrigem = origem.getSaldo();
                if (saldoOrigem.compareTo(valor) < 0) {
                    throw new IllegalStateException("Saldo insuficiente para realizar a transferência.");
                }
                BigDecimal saldoDestino = destino.getSaldo();
                BigDecimal novoSaldoOrigem = saldoOrigem.subtract(valor);
                BigDecimal novoSaldoDestino = saldoDestino.add(valor);
                Transacao transacao = criarTransacao(Transacao.Tipo.TRANSFERENCIA, valor, origemId, destinoId);
                repositorio.transferir(origemId, saldoOrigem, novoSaldoOrigem,
                        destinoId, saldoDestino, novoSaldoDestino, transacao);
                origem.sacar(valor);
                destino.depositar(valor);
                return transacao;
            }
        }
    }

    public List<Transacao> consultarExtrato(UUID contaId) {
        obterConta(contaId);
        return repositorio.listarTransacoes(contaId);
    }

    @Override
    public Path exportarExtrato(UUID contaId, Path arquivo) {
        return new ExportadorExtrato(this).exportar(contaId, arquivo);
    }

    public Path getArquivoBanco() { return repositorio.getConexaoBanco().getArquivo(); }

    private static Transacao criarTransacao(Transacao.Tipo tipo, BigDecimal valor, UUID origem, UUID destino) {
        return new Transacao(UUID.randomUUID(), tipo, valor, LocalDateTime.now(), origem, destino);
    }
}
