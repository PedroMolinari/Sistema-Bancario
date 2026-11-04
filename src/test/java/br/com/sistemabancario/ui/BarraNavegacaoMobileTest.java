package br.com.sistemabancario.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BarraNavegacaoMobileTest {
    @Test
    void botoesAbremAsCincoAreasPrincipais() throws Exception {
        List<String> destinos = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            BarraNavegacaoMobile barra = new BarraNavegacaoMobile(destinos::add);
            for (String rota : List.of("inicio", "contas", "operacoes", "extrato", "chat")) {
                assertNotNull(barra.botao(rota));
                barra.botao(rota).doClick();
            }
        });
        assertEquals(List.of("inicio", "contas", "operacoes", "extrato", "chat"), destinos);
    }

    @Test
    void destacaAbaCorrespondenteAsTelasSecundarias() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BarraNavegacaoMobile barra = new BarraNavegacaoMobile(destino -> { });
            barra.selecionar("transferencia");
            assertEquals(EstiloSwing.AZUL_ATIVO, barra.botao("operacoes").getForeground());
            assertEquals(EstiloSwing.SECUNDARIO, barra.botao("extrato").getForeground());
            barra.selecionar("cadastro");
            assertEquals(EstiloSwing.AZUL_ATIVO, barra.botao("contas").getForeground());
            barra.selecionar("chat");
            assertEquals(EstiloSwing.AZUL_ATIVO, barra.botao("chat").getForeground());
        });
    }

    @Test
    void atalhosDeAcoesMantemDepositoSaqueETransferenciaAcessiveis() throws Exception {
        List<String> destinos = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            PainelOperacoes operacoes = new PainelOperacoes(destinos::add);
            for (String rotulo : List.of("Depositar", "Sacar", "Transferir")) {
                encontrarBotao(operacoes, rotulo).doClick();
            }
        });
        assertEquals(List.of("deposito", "saque", "transferencia"), destinos);
    }

    private static JButton encontrarBotao(Component raiz, String texto) {
        if (raiz instanceof JButton botao && texto.equals(botao.getText())) return botao;
        if (raiz instanceof Container container) {
            for (Component filho : container.getComponents()) {
                JButton resultado = encontrarBotao(filho, texto);
                if (resultado != null) return resultado;
            }
        }
        return null;
    }
}
