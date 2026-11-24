package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssistenteBancarioServiceTest {
    private final UUID idConta = UUID.fromString("00000000-0000-0000-0000-00000000002a");
    private final UUID idDestino = UUID.fromString("00000000-0000-0000-0000-00000000002b");
    private FakeConsultas consultas;
    private AssistenteBancarioService assistente;
    private Conta conta;
    private Transacao transacao;

    @BeforeEach
    void preparar() {
        conta = new Conta(idConta, new Cliente("Titular fictício"), new BigDecimal("123.45"));
        Conta destino = new Conta(idDestino, new Cliente("Outro titular fictício"), BigDecimal.ZERO);
        transacao = new Transacao(UUID.randomUUID(), Transacao.Tipo.TRANSFERENCIA,
                new BigDecimal("12.34"), LocalDateTime.of(2025, 2, 3, 4, 5), idConta, idDestino);
        consultas = new FakeConsultas(List.of(conta, destino), Map.of(idConta, List.of(transacao)));
        assistente = new AssistenteBancarioService(consultas);
    }

    @Test
    void listaTodasContasComIdentificadorCurtoESaldoSemNome() {
        List<AssistenteBancarioService.ContaResumo> resultado = assistente.listarContas();

        assertEquals(2, resultado.size());
        assertEquals(conta.getIdentificadorCurto(), resultado.getFirst().identificador());
        assertEquals(0, new BigDecimal("123.45").compareTo(resultado.getFirst().saldo()));
        assertFalse(resultado.toString().contains("Titular fictício"));
        assertFalse(resultado.getFirst().identificador().contains(idConta.toString()));
    }

    @Test
    void consultaSaldoDaContaIdentificadaPeloCodigoCurto() {
        assertEquals(0, new BigDecimal("123.45").compareTo(
                assistente.consultarSaldo(conta.getIdentificadorCurto())));
    }

    @Test
    void consultaExtratoEDevolveSomenteDadosMinimizados() {
        List<AssistenteBancarioService.MovimentacaoResumo> resultado =
                assistente.consultarExtrato(conta.getIdentificadorCurto());

        assertEquals(1, resultado.size());
        var movimento = resultado.getFirst();
        assertEquals(Transacao.Tipo.TRANSFERENCIA, movimento.tipo());
        assertEquals(0, new BigDecimal("12.34").compareTo(movimento.valor()));
        assertEquals(transacao.dataHora(), movimento.dataHora());
        assertEquals(Conta.formatarIdentificador(idConta), movimento.contaOrigem());
        assertEquals(Conta.formatarIdentificador(idDestino), movimento.contaDestino());
        assertFalse(movimento.toString().contains(idConta.toString()));
    }

    @Test
    void rejeitaIdentificadorNulo() {
        assertThrows(NullPointerException.class, () -> assistente.consultarSaldo(null));
        assertThrows(NullPointerException.class, () -> assistente.consultarExtrato(null));
    }

    @Test
    void propagaContaInexistenteComExcecaoDoServicoBancario() {
        String inexistente = Conta.formatarIdentificador(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> assistente.consultarSaldo(inexistente));
        assertThrows(IllegalArgumentException.class, () -> assistente.consultarExtrato(inexistente));
    }

    @Test
    void rejeitaFormatoDeIdentificadorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> assistente.consultarSaldo("não-é-um-id"));
    }

    @Test
    void interfacePublicaDoAssistenteNaoPossuiOperacoesDeEscrita() {
        assertEquals(List.of("consultarExtrato", "consultarSaldo", "listarContas"),
                java.util.Arrays.stream(AssistenteBancarioService.class.getDeclaredMethods())
                        .filter(metodo -> java.lang.reflect.Modifier.isPublic(metodo.getModifiers()))
                        .map(java.lang.reflect.Method::getName)
                        .sorted()
                        .toList());
    }

    private static final class FakeConsultas implements ConsultaBancariaPorta {
        private final List<Conta> contas;
        private final Map<UUID, List<Transacao>> extratos;

        private FakeConsultas(List<Conta> contas, Map<UUID, List<Transacao>> extratos) {
            this.contas = List.copyOf(contas);
            this.extratos = Map.copyOf(extratos);
        }

        @Override public List<Conta> listarContas() { return contas; }

        @Override public Conta obterConta(UUID id) {
            return contas.stream().filter(conta -> conta.getIdentificador().equals(id)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + id));
        }

        @Override public List<Transacao> consultarExtrato(UUID contaId) {
            obterConta(contaId);
            return extratos.getOrDefault(contaId, List.of());
        }
    }
}
