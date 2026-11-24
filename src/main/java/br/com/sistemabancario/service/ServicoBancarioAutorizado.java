package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.PerfilAcesso;
import br.com.sistemabancario.domain.Transacao;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Fachada de casos de uso que aplica o perfil e a titularidade em toda consulta e operação. */
public final class ServicoBancarioAutorizado implements ServicoBancarioPorta {
    private final ServicoBancario banco;
    private final AutenticacaoService autenticacao;
    private final SessaoBancaria sessao;

    public ServicoBancarioAutorizado(ServicoBancario banco, AutenticacaoService autenticacao,
                                     SessaoBancaria sessao) {
        this.banco = java.util.Objects.requireNonNull(banco);
        this.autenticacao = java.util.Objects.requireNonNull(autenticacao);
        this.sessao = java.util.Objects.requireNonNull(sessao);
        autenticacao.exigirIdentidade(sessao);
    }

    public PerfilAcesso perfil() { return identidade().perfil(); }
    public UUID clienteId() { return identidade().clienteId(); }

    /** Porta de consulta de escopo cliente; sessões administrativas não podem ser conectadas ao assistente. */
    public ConsultaBancariaPorta consultasParaAssistente() {
        if (perfil() != PerfilAcesso.CLIENTE) {
            throw new SecurityException("O assistente só pode receber consultas de uma sessão de cliente.");
        }
        return new ConsultaBancariaPorta() {
            @Override public List<Conta> listarContas() { return ServicoBancarioAutorizado.this.listarContas(); }
            @Override public Conta obterConta(UUID id) { return ServicoBancarioAutorizado.this.obterConta(id); }
            @Override public List<Transacao> consultarExtrato(UUID id) {
                return ServicoBancarioAutorizado.this.consultarExtrato(id);
            }
        };
    }

    public List<Conta> listarContas() {
        AutenticacaoService.Identidade identidade = identidade();
        List<Conta> contas = banco.listarContas();
        return (identidade.perfil() == PerfilAcesso.ADMINISTRADOR ? contas
                : contas.stream().filter(conta -> conta.getTitular().getIdentificador().equals(identidade.clienteId()))
                        .toList()).stream().map(ServicoBancarioAutorizado::copia).toList();
    }

    public Conta obterConta(UUID id) {
        Conta conta = banco.obterConta(id);
        validarLeitura(conta);
        return copia(conta);
    }

    @Override public Conta criarConta(Cliente cliente, BigDecimal saldoInicial) {
        throw new SecurityException("A conta precisa ser criada pelo cadastro de acesso de cliente.");
    }

    @Override public void excluirConta(UUID id) {
        exigirAdministrador();
        banco.excluirConta(id);
    }

    @Override public Transacao depositar(UUID contaId, BigDecimal valor) {
        exigirClienteProprietario(contaId);
        return banco.depositar(contaId, valor);
    }

    @Override public Transacao sacar(UUID contaId, BigDecimal valor) {
        exigirClienteProprietario(contaId);
        return banco.sacar(contaId, valor);
    }

    @Override public Transacao transferir(UUID origemId, UUID destinoId, BigDecimal valor) {
        exigirClienteProprietario(origemId);
        banco.obterConta(destinoId);
        return banco.transferir(origemId, destinoId, valor);
    }

    @Override public Transacao transferirPorNumeroPublico(UUID origemId, long destinoNumero, BigDecimal valor) {
        exigirClienteProprietario(origemId);
        if (destinoNumero <= 0) throw new IllegalArgumentException("Número de conta de destino inválido.");
        Conta destino = banco.obterContaPorNumeroPublico(destinoNumero);
        return banco.transferir(origemId, destino.getIdentificador(), valor);
    }

    public List<Transacao> consultarExtrato(UUID contaId) {
        validarLeitura(banco.obterConta(contaId));
        return banco.consultarExtrato(contaId);
    }

    @Override public List<Transacao> consultarMovimentacoesRecentes(int limite) {
        if (limite < 0) throw new IllegalArgumentException("O limite não pode ser negativo.");
        if (perfil() == PerfilAcesso.ADMINISTRADOR) return banco.consultarMovimentacoesRecentes(limite);
        return listarContas().stream().flatMap(conta -> banco.consultarExtrato(conta.getIdentificador()).stream())
                .sorted(Comparator.comparing(Transacao::dataHora).reversed())
                .limit(limite).toList();
    }

    @Override public Path exportarExtrato(UUID contaId, Path arquivo) {
        validarLeitura(banco.obterConta(contaId));
        return banco.exportarExtrato(contaId, arquivo);
    }

    private void validarLeitura(Conta conta) {
        AutenticacaoService.Identidade identidade = identidade();
        if (identidade.perfil() == PerfilAcesso.ADMINISTRADOR) return;
        if (!conta.getTitular().getIdentificador().equals(identidade.clienteId())) {
            throw new IllegalArgumentException("Conta não encontrada.");
        }
    }

    private void exigirClienteProprietario(UUID contaId) {
        AutenticacaoService.Identidade identidade = identidade();
        if (identidade.perfil() != PerfilAcesso.CLIENTE) {
            throw new SecurityException("Operações bancárias exigem uma sessão de cliente.");
        }
        Conta conta = banco.obterConta(contaId);
        if (!conta.getTitular().getIdentificador().equals(identidade.clienteId())) {
            throw new IllegalArgumentException("Conta não encontrada.");
        }
    }

    private void exigirAdministrador() {
        if (perfil() != PerfilAcesso.ADMINISTRADOR) {
            throw new SecurityException("Esta ação exige perfil de administrador.");
        }
    }

    private AutenticacaoService.Identidade identidade() { return autenticacao.exigirIdentidade(sessao); }

    private static Conta copia(Conta conta) {
        Cliente titular = new Cliente(conta.getTitular().getIdentificador(), conta.getTitular().getNome());
        return new Conta(conta.getIdentificador(), titular, conta.getSaldo(), conta.getNumeroPublico());
    }
}
