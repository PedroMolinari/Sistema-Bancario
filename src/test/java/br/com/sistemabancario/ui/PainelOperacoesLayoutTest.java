package br.com.sistemabancario.ui;

import java.awt.Component;
import java.awt.Container;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelOperacoesLayoutTest {
    @Test
    void formatacaoMonetariaMantemPrecisaoESeparadoresAceitos() {
        assertEquals("1234,50", EstiloSwing.formatarValorMonetario("1234.5"));
        assertEquals("0,000001", EstiloSwing.formatarValorMonetario("0.000001"));
        assertNull(EstiloSwing.formatarValorMonetario("valor inválido"));
    }

    @Test
    void formulariosCompactosEGradeSeAdaptaALargura() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BiConsumer<br.com.sistemabancario.domain.Conta, BigDecimal> acao = (conta, valor) -> { };
            PainelOperacao deposito = new PainelOperacao(PainelOperacao.Tipo.DEPOSITO, acao, true);
            PainelOperacao saque = new PainelOperacao(PainelOperacao.Tipo.SAQUE, acao, true);
            PainelTransferencia transferencia = new PainelTransferencia((origem, destino, valor) -> { }, true);
            PainelOperacoes painel = new PainelOperacoes(deposito, saque, transferencia);
            assertTrue(deposito.getPreferredSize().height < 240, "Formulário compacto deve caber em até 240 px");
            assertTrue(saque.getPreferredSize().height < 240);
            assertTrue(transferencia.getPreferredSize().height < 300,
                    "Cartão de transferência compacto: " + transferencia.getPreferredSize().height + " px");

            painel.setSize(1200, 600);
            painel.atualizarLayoutResponsivo();
            painel.doLayout();
            JPanel grade = (JPanel) painel.getComponent(1);
            grade.setSize(painel.getWidth(), painel.getHeight() - 80);
            grade.doLayout();
            assertTrue(saque.getX() > deposito.getX());
            assertTrue(transferencia.getX() > saque.getX(), "Em janela larga, os três cartões usam três colunas");
            assertEquals(deposito.getHeight(), saque.getHeight());
            assertEquals(saque.getHeight(), transferencia.getHeight(),
                    "Os três cartões devem ficar com alturas equilibradas na grade larga");

            painel.setSize(1000, 600);
            painel.atualizarLayoutResponsivo();
            painel.doLayout();
            grade.setSize(painel.getWidth(), painel.getHeight() - 80);
            grade.doLayout();
            assertEquals(deposito.getY(), transferencia.getY(),
                    "Por volta de 1000 px, os formulários devem usar duas colunas para evitar corte.");
            assertTrue(transferencia.getX() > deposito.getX());
            assertTrue(saque.getY() > deposito.getY());

            painel.setSize(800, 600);
            painel.atualizarLayoutResponsivo();
            painel.doLayout();
            grade.setSize(painel.getWidth(), painel.getHeight() - 80);
            grade.doLayout();
            assertEquals(deposito.getY(), transferencia.getY(),
                    "Em duas colunas, a transferência acompanha o primeiro par de operações");
            assertTrue(saque.getY() > deposito.getY());

            painel.setSize(620, 600);
            painel.atualizarLayoutResponsivo();
            painel.doLayout();
            grade.setSize(painel.getWidth(), painel.getHeight() - 80);
            grade.doLayout();
            assertTrue(deposito.getY() < transferencia.getY());
            assertTrue(saque.getY() < transferencia.getY(),
                    "Em uma coluna, os formulários mantêm a ordem depósito, saque e transferência");
        });
    }

    @Test
    void atalhoSelecionaERealcaFormularioSemExecutarOperacao() throws Exception {
        java.util.concurrent.atomic.AtomicInteger execucoes = new java.util.concurrent.atomic.AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            PainelOperacao deposito = new PainelOperacao(PainelOperacao.Tipo.DEPOSITO,
                    (conta, valor) -> execucoes.incrementAndGet(), true);
            PainelOperacao saque = new PainelOperacao(PainelOperacao.Tipo.SAQUE,
                    (conta, valor) -> execucoes.incrementAndGet(), true);
            PainelTransferencia transferencia = new PainelTransferencia(
                    (origem, destino, valor) -> execucoes.incrementAndGet(), true);
            PainelOperacoes painel = new PainelOperacoes(deposito, saque, transferencia);
            painel.selecionar("transferencia");
            assertEquals("transferencia", painel.operacaoSelecionada());
            painel.selecionar("deposito");
            assertEquals("deposito", painel.operacaoSelecionada());
            painel.selecionar("contas");
            assertNull(painel.operacaoSelecionada());
        });
        assertEquals(0, execucoes.get(), "Atalhos só abrem e destacam o formulário.");
    }

    @Test
    void controlesMantemAlturaCompactaSemEsticarCampos() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelOperacao deposito = new PainelOperacao(PainelOperacao.Tipo.DEPOSITO,
                    (conta, valor) -> { }, true);
            deposito.setSize(360, 230);
            layout(deposito);
            assertTrue(deposito.getPreferredSize().height < 240);
            assertTrue(alturaMaximaControles(deposito) <= 42,
                    "Controles devem permanecer compactos; altura máxima encontrada: "
                            + alturaMaximaControles(deposito));
        });
    }

    private static int alturaMaximaControles(Component componente) {
        int altura = componente instanceof javax.swing.JComboBox<?> || componente instanceof javax.swing.JTextField
                || componente instanceof javax.swing.JButton ? componente.getPreferredSize().height : 0;
        if (componente instanceof Container container) {
            for (Component filho : container.getComponents()) {
                altura = Math.max(altura, alturaMaximaControles(filho));
            }
        }
        return altura;
    }

    private static void layout(Component componente) {
        if (componente instanceof Container container) {
            container.doLayout();
            for (Component filho : container.getComponents()) layout(filho);
        }
    }
}
