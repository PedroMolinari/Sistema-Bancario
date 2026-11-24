package br.com.sistemabancario.ui;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelAcessoTest {
    @Test
    void acessoAlternaEntreComposicaoDesktopECompacta() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelAcesso painel = new PainelAcesso(() -> { });
            painel.setSize(1280, 820);
            layoutTree(painel);
            assertTrue(painel.modoAmplo());
            painel.setSize(420, 680);
            layoutTree(painel);
            assertFalse(painel.modoAmplo());
            assertTrue(painel.botaoEntrar().getWidth() > 0);
        });
    }

    @Test
    void exibeEntradaGeralSemCredenciaisEAcionaAberturaDoAplicativo() throws Exception {
        boolean[] entrou = {false};
        SwingUtilities.invokeAndWait(() -> {
            PainelAcesso painel = new PainelAcesso(() -> entrou[0] = true);
            painel.setSize(390, 760);
            layoutTree(painel);
            assertEquals("Entrar no aplicativo", painel.botaoEntrar().getText());
            assertFalse(contemCampoCredencial(painel));
            assertTrue(painel.botaoEntrar().getWidth() > 0);
            assertTrue(painel.botaoEntrar().getWidth() <= painel.getWidth());
            painel.botaoEntrar().doClick();
        });
        assertTrue(entrou[0]);
    }

    @Test
    void painelRaizAlternaDaEntradaParaAplicativoEAceitaRetorno() throws Exception {
        boolean[] iniciou = {false};
        SwingUtilities.invokeAndWait(() -> {
            PainelRaizMobile raiz = new PainelRaizMobile(new javax.swing.JPanel(), () -> iniciou[0] = true);
            assertFalse(raiz.aplicativoVisivel());
            raiz.botaoEntrar().doClick();
            assertTrue(raiz.aplicativoVisivel());
            assertTrue(iniciou[0]);
            raiz.mostrarAcesso();
            assertFalse(raiz.aplicativoVisivel());
        });
    }

    private static boolean contemCampoCredencial(Component componente) {
        if (componente instanceof JTextField || componente instanceof JPasswordField) return true;
        if (componente instanceof Container container) {
            for (Component filho : container.getComponents()) {
                if (contemCampoCredencial(filho)) return true;
            }
        }
        return false;
    }

    private static void layoutTree(Container raiz) {
        raiz.doLayout();
        for (Component filho : raiz.getComponents()) {
            if (filho instanceof Container container) layoutTree(container);
        }
    }
}
