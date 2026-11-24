package br.com.sistemabancario.ui;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelInicioAcoesTest {
    @Test
    void atalhosSaoCartoesCompactosComIconeAcessivelEClicavel() throws Exception {
        AtomicReference<String> destino = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            PainelInicio painel = new PainelInicio(destino::set);
            painel.setSize(1100, 760);
            painel.doLayout();
            List<JButton> botoes = encontrar(painel);
            for (String titulo : List.of("Depósito", "Saque", "Transferência", "Minhas contas", "Extrato", "Assistente")) {
                JButton botao = botoes.stream().filter(item -> titulo.equals(item.getText())).findFirst().orElseThrow();
                assertNotNull(botao.getIcon(), "Ícone ausente em " + titulo);
                assertEquals(titulo, botao.getAccessibleContext().getAccessibleName());
                assertTrue(botao.getPreferredSize().width <= 170);
                assertTrue(botao.getPreferredSize().height <= 90);
                String rota = switch (titulo) {
                    case "Depósito" -> "deposito";
                    case "Saque" -> "saque";
                    case "Transferência" -> "transferencia";
                    case "Minhas contas" -> "contas";
                    case "Extrato" -> "extrato";
                    default -> "chat";
                };
                botao.doClick();
                assertEquals(rota, destino.get(), "Destino incorreto para " + titulo);
            }
        });
        assertEquals("chat", destino.get(), "O atalho de Assistente deve abrir a aba de chat.");
    }

    private static List<JButton> encontrar(java.awt.Component raiz) {
        java.util.ArrayList<JButton> resultado = new java.util.ArrayList<>();
        if (raiz instanceof JButton botao) resultado.add(botao);
        if (raiz instanceof java.awt.Container container) {
            for (java.awt.Component filho : container.getComponents()) resultado.addAll(encontrar(filho));
        }
        return resultado;
    }
}
