package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import br.com.sistemabancario.service.ServicoBancarioPorta;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ControladorBancarioTest {
    @TempDir Path diretorioTemporario;
    private static final UUID CONTA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DESTINO_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final Cliente CLIENTE = new Cliente("Ana");
    private static final BigDecimal VALOR = new BigDecimal("25.40");

    @Test
    void encaminhaListagemEConsultaDeConta() {
        ServicoFalso servico = new ServicoFalso();
        Conta conta = new Conta(CONTA_ID, CLIENTE, new BigDecimal("100.00"));
        servico.contas = List.of(conta);
        servico.conta = conta;
        ControladorBancario controlador = new ControladorBancario(servico);

        assertSame(servico.contas, controlador.listarContas());
        assertSame(conta, controlador.obterConta(CONTA_ID));
        assertEquals(1, servico.chamadasListarContas);
        assertEquals(CONTA_ID, servico.idConsultado);
    }

    @Test
    void encaminhaCadastroComClienteESaldoInicial() {
        ServicoFalso servico = new ServicoFalso();
        Conta conta = new Conta(CLIENTE, VALOR);
        servico.conta = conta;
        ControladorBancario controlador = new ControladorBancario(servico);

        assertSame(conta, controlador.criarConta(CLIENTE, VALOR));
        assertSame(CLIENTE, servico.clienteRecebido);
        assertEquals(0, VALOR.compareTo(servico.valorRecebido));
    }

    @Test
    void encaminhaDepositoComContaEValorCorretos() {
        ServicoFalso servico = new ServicoFalso();
        Transacao transacao = transacao(Transacao.Tipo.DEPOSITO, CONTA_ID, null);
        servico.transacao = transacao;
        ControladorBancario controlador = new ControladorBancario(servico);

        assertSame(transacao, controlador.depositar(CONTA_ID, VALOR));
        assertEquals(CONTA_ID, servico.idRecebido);
        assertEquals(0, VALOR.compareTo(servico.valorRecebido));
    }

    @Test
    void encaminhaSaqueEPropagaRejeicaoDoServico() {
        ServicoFalso servico = new ServicoFalso();
        IllegalStateException rejeicao = new IllegalStateException("Saldo insuficiente.");
        servico.falha = rejeicao;
        ControladorBancario controlador = new ControladorBancario(servico);

        IllegalStateException recebida = assertThrows(IllegalStateException.class,
                () -> controlador.sacar(CONTA_ID, VALOR));

        assertSame(rejeicao, recebida);
        assertEquals(CONTA_ID, servico.idRecebido);
        assertEquals(0, VALOR.compareTo(servico.valorRecebido));
    }

    @Test
    void encaminhaTransferenciaComOrigemDestinoEValorCorretos() {
        ServicoFalso servico = new ServicoFalso();
        Transacao transacao = transacao(Transacao.Tipo.TRANSFERENCIA, CONTA_ID, DESTINO_ID);
        servico.transacao = transacao;
        ControladorBancario controlador = new ControladorBancario(servico);

        assertSame(transacao, controlador.transferir(CONTA_ID, DESTINO_ID, VALOR));
        assertEquals(CONTA_ID, servico.origemRecebida);
        assertEquals(DESTINO_ID, servico.destinoRecebido);
        assertEquals(0, VALOR.compareTo(servico.valorRecebido));
    }

    @Test
    void encaminhaConsultaDeExtratoParaContaSelecionada() {
        ServicoFalso servico = new ServicoFalso();
        List<Transacao> extrato = List.of(transacao(Transacao.Tipo.DEPOSITO, CONTA_ID, null));
        servico.extrato = extrato;
        ControladorBancario controlador = new ControladorBancario(servico);

        assertSame(extrato, controlador.consultarExtrato(CONTA_ID));
        assertEquals(CONTA_ID, servico.idConsultado);
    }

    @Test
    void exportaExtratoPeloServicoFalsoSemSQLite() throws Exception {
        ServicoFalso servico = new ServicoFalso();
        servico.conta = new Conta(CONTA_ID, CLIENTE, new BigDecimal("100.00"));
        servico.extrato = List.of(transacao(Transacao.Tipo.DEPOSITO, CONTA_ID, null));
        Path arquivo = diretorioTemporario.resolve("extrato.txt");

        Path exportado = new ControladorBancario(servico).exportarExtrato(CONTA_ID, arquivo);

        assertEquals(arquivo.toAbsolutePath().normalize(), exportado);
        assertTrue(Files.readString(exportado).contains("Titular: Ana"));
        assertTrue(Files.readString(exportado).contains("DEPOSITO"));
        assertEquals(CONTA_ID, servico.idConsultado);
    }

    private static Transacao transacao(Transacao.Tipo tipo, UUID origem, UUID destino) {
        return new Transacao(UUID.randomUUID(), tipo, VALOR, LocalDateTime.now(), origem, destino);
    }

    private static final class ServicoFalso implements ServicoBancarioPorta {
        private List<Conta> contas = List.of();
        private List<Transacao> extrato = List.of();
        private Conta conta;
        private Transacao transacao;
        private Cliente clienteRecebido;
        private BigDecimal valorRecebido;
        private UUID idConsultado;
        private UUID idRecebido;
        private UUID origemRecebida;
        private UUID destinoRecebido;
        private RuntimeException falha;
        private int chamadasListarContas;

        @Override
        public List<Conta> listarContas() {
            chamadasListarContas++;
            return contas;
        }

        @Override
        public Conta obterConta(UUID id) {
            idConsultado = id;
            return conta;
        }

        @Override
        public Conta criarConta(Cliente cliente, BigDecimal saldoInicial) {
            clienteRecebido = cliente;
            valorRecebido = saldoInicial;
            return conta;
        }

        @Override
        public Transacao depositar(UUID contaId, BigDecimal valor) {
            idRecebido = contaId;
            valorRecebido = valor;
            if (falha != null) throw falha;
            return transacao;
        }

        @Override
        public Transacao sacar(UUID contaId, BigDecimal valor) {
            idRecebido = contaId;
            valorRecebido = valor;
            if (falha != null) throw falha;
            return transacao;
        }

        @Override
        public Transacao transferir(UUID origemId, UUID destinoId, BigDecimal valor) {
            origemRecebida = origemId;
            destinoRecebido = destinoId;
            valorRecebido = valor;
            if (falha != null) throw falha;
            return transacao;
        }

        @Override
        public List<Transacao> consultarExtrato(UUID contaId) {
            idConsultado = contaId;
            return extrato;
        }

        @Override
        public Path exportarExtrato(UUID contaId, Path arquivo) {
            return new br.com.sistemabancario.service.ExportadorExtrato(this).exportar(contaId, arquivo);
        }
    }
}
