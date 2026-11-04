package br.com.sistemabancario.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelChatTest {
    @Test
    void conversaFicaCompactaRolavelEControlesPermanecemNaTela() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelChat chat = new PainelChat(mensagem -> "Resposta de teste");
            chat.setSize(390, 680);
            layoutTree(chat);
            JScrollPane conversa = encontrarTodos(chat, JScrollPane.class).stream()
                    .filter(painel -> painel.getViewport().getView() instanceof JTextArea)
                    .findFirst().orElseThrow();
            assertEquals(230, conversa.getPreferredSize().height);
            assertEquals(230, conversa.getMaximumSize().height);
            assertTrue(conversa.getVerticalScrollBarPolicy() == JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            assertTrue(encontrarTodos(chat, JTextField.class).stream().anyMatch(campo -> campo.getWidth() > 0));
            JButton enviar = encontrarTodos(chat, JButton.class).stream()
                    .filter(botao -> "Enviar".equals(botao.getText())).findFirst().orElseThrow();
            assertTrue(enviar.getWidth() > 0);
            assertTrue(chat.getPreferredSize().height < 500,
                    "O cartão do chat não deve crescer para ocupar quase toda a janela.");
        });
    }

    private static <T extends Component> List<T> encontrarTodos(Component raiz, Class<T> tipo) {
        ArrayList<T> componentes = new ArrayList<>();
        coletar(raiz, tipo, componentes);
        return componentes;
    }

    private static <T extends Component> void coletar(Component raiz, Class<T> tipo, List<T> encontrados) {
        if (tipo.isInstance(raiz)) encontrados.add(tipo.cast(raiz));
        if (raiz instanceof Container container) {
            for (Component filho : container.getComponents()) coletar(filho, tipo, encontrados);
        }
    }

    private static void layoutTree(Component componente) {
        if (componente instanceof Container container) {
            container.doLayout();
            for (Component filho : container.getComponents()) layoutTree(filho);
        }
    }
}
