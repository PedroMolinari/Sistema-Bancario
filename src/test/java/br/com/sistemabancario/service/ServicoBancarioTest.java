package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.exception.PersistenciaException;
import br.com.sistemabancario.repository.jdbc.ConexaoBanco;
import br.com.sistemabancario.simulacao.SimuladorConcorrencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class ServicoBancarioTest {
    @TempDir Path diretorioTemporario;

    @Test
    void persisteSaldosETransacoesERejeitaOperacoesInvalidas() {
        Path arquivo = diretorioTemporario.resolve("persistencia.db");
        ServicoBancario banco = new ServicoBancario(arquivo);
        Conta origem = banco.criarConta(new Cliente("Ana"), new BigDecimal("100.00"));
        Conta destino = banco.criarConta(new Cliente("Bia"), BigDecimal.ZERO);

        banco.depositar(origem.getIdentificador(), new BigDecimal("20.00"));
        banco.sacar(origem.getIdentificador(), new BigDecimal("5.00"));
        banco.transferir(origem.getIdentificador(), destino.getIdentificador(), new BigDecimal("15.00"));
        assertThrows(IllegalStateException.class,
                () -> banco.sacar(origem.getIdentificador(), new BigDecimal("1000.00")));
        assertThrows(IllegalArgumentException.class,
                () -> banco.depositar(origem.getIdentificador(), BigDecimal.ZERO));

        ServicoBancario recarregado = new ServicoBancario(arquivo);
        assertSaldo("100.00", recarregado.obterConta(origem.getIdentificador()));
        assertSaldo("15.00", recarregado.obterConta(destino.getIdentificador()));
        assertEquals(3, recarregado.consultarExtrato(origem.getIdentificador()).size());
        assertEquals(1, recarregado.consultarExtrato(destino.getIdentificador()).size());
    }

    @Test
    void depositosConcorrentesPersistidosMantemSaldoEsperado() {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("depositos.db"));
        Conta conta = banco.criarConta(new Cliente("Concorrente"), new BigDecimal("10.00"));
        SimuladorConcorrencia.Resultado resultado = new SimuladorConcorrencia()
                .depositosConcorrentes(banco, conta.getIdentificador(), 6, 40, new BigDecimal("0.10"));
        assertSaldo("34.00", banco.obterConta(conta.getIdentificador()));
        assertEquals(0, resultado.esperado().compareTo(resultado.observado()));
        assertEquals(240, banco.consultarExtrato(conta.getIdentificador()).size());
    }

    @Test
    void transferenciasOpostasTerminamSemDeadlockEConservamSaldos() {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("transferencias.db"));
        Conta a = banco.criarConta(new Cliente("A"), new BigDecimal("100.00"));
        Conta b = banco.criarConta(new Cliente("B"), new BigDecimal("100.00"));
        SimuladorConcorrencia.ResultadoTransferencias resultado = new SimuladorConcorrencia()
                .transferenciasOpostas(banco, a.getIdentificador(), b.getIdentificador(), 60, new BigDecimal("0.01"));
        assertSaldo("100.00", banco.obterConta(a.getIdentificador()));
        assertSaldo("100.00", banco.obterConta(b.getIdentificador()));
        assertEquals(0, resultado.totalAntes().compareTo(resultado.totalDepois()));
        assertEquals(120, banco.consultarExtrato(a.getIdentificador()).size());
        assertEquals(120, banco.consultarExtrato(b.getIdentificador()).size());
    }

    @Test
    void demonstracaoControladaExibeAtualizacaoPerdida() {
        SimuladorConcorrencia.Resultado resultado = new SimuladorConcorrencia().demonstrarRaceCondition(8);
        assertEquals(0, new BigDecimal("8").compareTo(resultado.esperado()));
        assertEquals(0, BigDecimal.ONE.compareTo(resultado.observado()));
    }

    @Test
    void falhaAoGravarTransacaoReverteDebitoECredito() throws Exception {
        Path arquivo = diretorioTemporario.resolve("rollback.db");
        ServicoBancario banco = new ServicoBancario(arquivo);
        Conta a = banco.criarConta(new Cliente("A"), new BigDecimal("50.00"));
        Conta b = banco.criarConta(new Cliente("B"), new BigDecimal("20.00"));
        try (var conexao = new ConexaoBanco(arquivo).abrir(); Statement comando = conexao.createStatement()) {
            comando.execute("CREATE TRIGGER falhar_transacao BEFORE INSERT ON transacao " +
                    "BEGIN SELECT RAISE(ABORT, 'falha de teste'); END");
        }

        assertThrows(PersistenciaException.class,
                () -> banco.transferir(a.getIdentificador(), b.getIdentificador(), new BigDecimal("5.00")));
        ServicoBancario recarregado = new ServicoBancario(arquivo);
        assertSaldo("50.00", recarregado.obterConta(a.getIdentificador()));
        assertSaldo("20.00", recarregado.obterConta(b.getIdentificador()));
        assertTrue(recarregado.consultarExtrato(a.getIdentificador()).isEmpty());
    }

    @Test
    void exportaExtratoLegivelComOperacoesPersistidas() throws Exception {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("extrato.db"));
        Conta conta = banco.criarConta(new Cliente("Carla"), BigDecimal.ZERO);
        banco.depositar(conta.getIdentificador(), new BigDecimal("12.34"));
        Path arquivo = diretorioTemporario.resolve("extrato.txt");

        Path gerado = new ExportadorExtrato(banco).exportar(conta.getIdentificador(), arquivo);
        String conteudo = Files.readString(gerado);
        assertTrue(conteudo.contains("EXTRATO BANCÁRIO"));
        assertTrue(conteudo.contains("Titular: Carla"));
        assertTrue(conteudo.contains("DEPOSITO | Crédito R$ 12.34"));
        assertTrue(conteudo.contains("Resultado: CONCLUÍDA"));
    }

    @Test
    void excluiSomenteContaSemMovimentacoesEAtualizaConsultas() {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("exclusao.db"));
        Conta vazia = banco.criarConta(new Cliente("Livre"), BigDecimal.ZERO);
        Conta movimentada = banco.criarConta(new Cliente("Movimentada"), BigDecimal.ZERO);
        banco.depositar(movimentada.getIdentificador(), BigDecimal.ONE);
        List<Conta> antes = banco.listarContas();

        banco.excluirConta(vazia.getIdentificador());
        assertEquals(1, banco.listarContas().size());
        assertThrows(IllegalArgumentException.class, () -> banco.obterConta(vazia.getIdentificador()));
        ServicoBancario recarregado = new ServicoBancario(banco.getArquivoBanco());
        assertEquals(1, recarregado.listarContas().size());
        assertThrows(IllegalArgumentException.class, () -> recarregado.obterConta(vazia.getIdentificador()));
        assertThrows(IllegalStateException.class, () -> banco.excluirConta(movimentada.getIdentificador()));
        assertEquals(1, banco.consultarExtrato(movimentada.getIdentificador()).size());
        assertEquals(2, antes.size());
    }

    @Test
    void exclusaoBloqueiaContasComSaqueEComoOrigemOuDestinoDaTransferencia() {
        ServicoBancario banco = new ServicoBancario(diretorioTemporario.resolve("exclusao-mov.db"));
        Conta saque = banco.criarConta(new Cliente("Saque"), BigDecimal.TEN);
        Conta origem = banco.criarConta(new Cliente("Origem"), BigDecimal.TEN);
        Conta destino = banco.criarConta(new Cliente("Destino"), BigDecimal.ZERO);
        banco.sacar(saque.getIdentificador(), BigDecimal.ONE);
        banco.transferir(origem.getIdentificador(), destino.getIdentificador(), BigDecimal.ONE);

        assertThrows(IllegalStateException.class, () -> banco.excluirConta(saque.getIdentificador()));
        assertThrows(IllegalStateException.class, () -> banco.excluirConta(origem.getIdentificador()));
        assertThrows(IllegalStateException.class, () -> banco.excluirConta(destino.getIdentificador()));
        assertEquals(1, banco.consultarExtrato(saque.getIdentificador()).size());
        assertEquals(1, banco.consultarExtrato(origem.getIdentificador()).size());
        assertEquals(1, banco.consultarExtrato(destino.getIdentificador()).size());
        assertEquals(3, banco.listarContas().size());
    }

    @Test
    void identificadoresCurtosSaoEstaveisEDistintos() {
        Conta a = new Conta(new Cliente("A"), BigDecimal.ZERO);
        Conta b = new Conta(new Cliente("B"), BigDecimal.ZERO);
        assertEquals(a.getIdentificadorCurto(), a.getIdentificadorCurto());
        assertNotEquals(a.getIdentificadorCurto(), b.getIdentificadorCurto());
        assertTrue(a.getIdentificadorCurto().length() <= 25);
    }

    private static void assertSaldo(String esperado, Conta conta) {
        assertEquals(0, new BigDecimal(esperado).compareTo(conta.getSaldo()));
    }
}
