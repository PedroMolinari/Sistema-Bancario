package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.PerfilAcesso;
import br.com.sistemabancario.domain.Conta;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ServicoBancarioAutorizadoTest {
    @TempDir Path diretorioTemporario;

    @Test
    void administradorNaoRecebeContaEConsultaTodasAsContasEoTotal() {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("admin.db"));
        AutenticacaoService autenticacao = new AutenticacaoService(banco);
        SessaoBancaria sessaoAdmin = autenticacao.registrarAdministradorInicial("Gestor Financeiro", "gestor",
                senha("Senha-admin-segura-2026"));
        ServicoBancarioAutorizado admin = new ServicoBancarioAutorizado(banco, autenticacao, sessaoAdmin);

        assertEquals(PerfilAcesso.ADMINISTRADOR, admin.perfil());
        assertTrue(admin.listarContas().isEmpty(), "O administrador não deve ganhar uma conta bancária própria.");
        assertThrows(SecurityException.class, admin::consultasParaAssistente,
                "Sessões administrativas não devem ser conectadas ao assistente.");

        registrar(autenticacao, "cliente1", "Carla", "Senha-cliente-segura-1", "120.00");
        registrar(autenticacao, "cliente2", "Davi", "Senha-cliente-segura-2", "80.00");
        assertEquals(2, admin.listarContas().size());
        BigDecimal total = admin.listarContas().stream().map(conta -> conta.getSaldo())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("200.00").compareTo(total));
        assertThrows(SecurityException.class, () -> admin.depositar(admin.listarContas().getFirst().getIdentificador(),
                BigDecimal.ONE));
        assertThrows(SecurityException.class, () -> admin.transferirPorNumeroPublico(
                admin.listarContas().getFirst().getIdentificador(), admin.listarContas().getLast().getNumeroPublico(),
                BigDecimal.ONE));
    }

    @Test
    void clienteSoConsultaSuasContasMasPodeTransferirParaNumeroPublicoDeDestino() {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("clientes.db"));
        AutenticacaoService autenticacao = new AutenticacaoService(banco);
        SessaoBancaria sessaoPropria = autenticacao.registrarCliente("Eva", "eva",
                senha("Senha-cliente-segura-1"), new BigDecimal("100.00"));
        SessaoBancaria sessaoAlheia = autenticacao.registrarCliente("Fábio", "fabio",
                senha("Senha-cliente-segura-2"), new BigDecimal("50.00"));
        Conta contaPropria = new ServicoBancarioAutorizado(banco, autenticacao, sessaoPropria)
                .listarContas().getFirst();
        Conta contaAlheia = new ServicoBancarioAutorizado(banco, autenticacao, sessaoAlheia)
                .listarContas().getFirst();
        var sessaoEva = autenticacao.autenticar("EVA", senha("Senha-cliente-segura-1"));
        ServicoBancarioAutorizado eva = new ServicoBancarioAutorizado(banco, autenticacao, sessaoEva);

        assertEquals(1, eva.listarContas().size());
        var assistente = new AssistenteBancarioService(eva.consultasParaAssistente());
        assertEquals(1, assistente.listarContas().size());
        assertEquals(contaPropria.getIdentificador(), eva.listarContas().getFirst().getIdentificador());
        assertThrows(IllegalArgumentException.class, () -> eva.obterConta(contaAlheia.getIdentificador()));
        assertThrows(IllegalArgumentException.class, () -> eva.consultarExtrato(contaAlheia.getIdentificador()));
        assertThrows(IllegalArgumentException.class, () -> eva.depositar(contaAlheia.getIdentificador(), BigDecimal.ONE));

        eva.obterConta(contaPropria.getIdentificador()).depositar(new BigDecimal("1000.00"));
        assertEquals(0, new BigDecimal("100.00").compareTo(
                eva.obterConta(contaPropria.getIdentificador()).getSaldo()),
                "A interface recebe cópias de leitura, não uma entidade mutável do serviço.");

        eva.transferirPorNumeroPublico(contaPropria.getIdentificador(), contaAlheia.getNumeroPublico(),
                new BigDecimal("10.00"));
        assertEquals(0, new BigDecimal("90.00").compareTo(eva.obterConta(contaPropria.getIdentificador()).getSaldo()));
        assertEquals(1, eva.consultarExtrato(contaPropria.getIdentificador()).size());
        assertEquals(0, new BigDecimal("60.00").compareTo(banco.obterConta(contaAlheia.getIdentificador()).getSaldo()));
    }

    @Test
    void primeiroAdministradorEUnicoEExigeSenhaMinimaDeDezCaracteres() {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("provisionamento.db"));
        AutenticacaoService autenticacao = new AutenticacaoService(banco);
        assertFalse(autenticacao.administradorConfigurado());
        assertThrows(IllegalArgumentException.class, () -> autenticacao.registrarAdministradorInicial(
                "Gestor", "gestor", senha("curta123")));
        assertThrows(IllegalArgumentException.class, () -> autenticacao.registrarCliente("Cliente", "cliente",
                senha("curta123"), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> autenticacao.registrarAdministradorInicial(
                "", "gestor", senha("Senha-segura-2026")));
        autenticacao.registrarAdministradorInicial("Gestor", "gestor", senha("Senha10ok!"));
        assertTrue(autenticacao.administradorConfigurado());
        assertThrows(IllegalStateException.class, () -> autenticacao.registrarAdministradorInicial(
                "Outro", "outro-admin", senha("Outra-senha-segura-2026")));
        assertNotNull(autenticacao.autenticar("gestor", senha("Senha10ok!")));
    }

    @Test
    void senhaPersistidaEVerificadaSemArmazenarTextoPuro() {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("credenciais.db"));
        AutenticacaoService autenticacao = new AutenticacaoService(banco);
        char[] senha = senha("Senha-cliente-segura-1");
        autenticacao.registrarCliente("Gabi", "gabi", senha, BigDecimal.ZERO);
        assertTrue(Arrays.equals(new char[senha.length], senha), "O serviço deve limpar o buffer de senha recebido.");
        assertNotNull(autenticacao.autenticar("gabi", senha("Senha-cliente-segura-1")));
        assertThrows(SecurityException.class, () -> autenticacao.autenticar("gabi", senha("Senha-errada-segura")));
    }

    private static void registrar(AutenticacaoService auth, String login, String nome, String senha, String saldo) {
        auth.registrarCliente(nome, login, senha(senha), new BigDecimal(saldo));
    }

    private static char[] senha(String valor) { return valor.toCharArray(); }
}
