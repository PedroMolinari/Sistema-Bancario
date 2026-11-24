package br.com.sistemabancario.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BarraNavegacaoLateralTest {
    @Test
    void lateralMantemDestinosTooltipsEAbaAtiva() throws Exception {
        List<String> destinos = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            BarraNavegacaoLateral barra = new BarraNavegacaoLateral(destinos::add);
            barra.selecionar("transferencia");
            assertEquals(EstiloSwing.ROSA_ATIVO, barra.botao("operacoes").getForeground());
            assertTrue(barra.botao("operacoes").getToolTipText().contains("Ações"));
            assertEquals("Abrir tela Extrato", barra.botao("extrato")
                    .getAccessibleContext().getAccessibleName());
            barra.botao("inicio").doClick();
            barra.botao("contas").doClick();
            barra.botao("operacoes").doClick();
            barra.botao("extrato").doClick();
            barra.botao("chat").doClick();
        });
        assertEquals(List.of("inicio", "contas", "operacoes", "extrato", "chat"), destinos);
    }

    @Test
    void perfilAdministradorOcultaOperacoesFinanceirasDaNavegacao() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BarraNavegacaoLateral barra = new BarraNavegacaoLateral(destino -> { });
            barra.configurarPerfil(true);
            assertFalse(barra.botao("operacoes").isVisible());
            assertTrue(barra.botao("contas").isVisible());
            assertTrue(barra.botao("extrato").isVisible());
        });
    }
}
