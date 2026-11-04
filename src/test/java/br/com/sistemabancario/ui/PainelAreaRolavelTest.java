package br.com.sistemabancario.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelAreaRolavelTest {
    @Test
    void telasCabemNaLarguraDaViewportEConteudoPermiteRolagemVertical() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CardLayout layout = new CardLayout();
            PainelAreaRolavel area = new PainelAreaRolavel(layout);
            area.add(new PainelInicio(destino -> { }), "inicio");
            area.add(new PainelContas(() -> { }, (destino, selecionada) -> { }, selecionada -> { }), "contas");
            area.add(new PainelOperacoes(destino -> { }), "operacoes");
            area.add(new PainelCadastroConta((nome, saldo) -> { }), "cadastro");
            area.add(new PainelOperacao(PainelOperacao.Tipo.DEPOSITO, (selecionada, valor) -> { }), "deposito");
            area.add(new PainelTransferencia((origem, destino, valor) -> { }), "transferencia");
            area.add(new PainelExtrato(selecionada -> { }, (selecionada, arquivo) -> { }), "extrato");
            area.add(new PainelChat(new ClienteChatSimulado()), "chat");

            JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(area,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
            rolagem.setSize(398, 680);
            rolagem.doLayout();
            rolagem.getViewport().doLayout();
            area.doLayout();
            int larguraViewport = rolagem.getViewport().getExtentSize().width;
            for (String tela : List.of("inicio", "contas", "operacoes", "cadastro",
                    "deposito", "transferencia", "extrato", "chat")) {
                layout.show(area, tela);
                area.doLayout();
                Component ativa = telaVisivel(area);
                layoutTree(ativa);
                assertEquals(larguraViewport, ativa.getWidth(), "Largura da tela " + tela);
                assertTrue(ativa.getHeight() > 0, "Altura da tela " + tela);
            }
            assertEquals(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, rolagem.getVerticalScrollBarPolicy());
            assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, rolagem.getHorizontalScrollBarPolicy());
            for (JScrollPane interna : encontrarTodos(area, JScrollPane.class)) {
                assertTrue(interna.isWheelScrollingEnabled());
                assertEquals(20, interna.getVerticalScrollBar().getUnitIncrement());
            }
        });
    }

    @Test
    void eventoDaRodaMoveConteudoVerticalDaViewport() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            javax.swing.JPanel conteudo = new javax.swing.JPanel();
            conteudo.setPreferredSize(new java.awt.Dimension(180, 900));
            JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(conteudo));
            rolagem.setSize(220, 240);
            rolagem.doLayout();
            rolagem.getViewport().doLayout();
            MouseWheelEvent roda = new MouseWheelEvent(rolagem, MouseEvent.MOUSE_WHEEL,
                    System.currentTimeMillis(), 0, 100, 100, 0, false,
                    MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, 3);
            rolagem.dispatchEvent(roda);
            assertTrue(rolagem.getViewport().getViewPosition().y > 0);
        });
    }

    private static <T extends Component> List<T> encontrarTodos(Component raiz, Class<T> tipo) {
        java.util.ArrayList<T> encontrados = new java.util.ArrayList<>();
        coletar(raiz, tipo, encontrados);
        return encontrados;
    }

    private static <T extends Component> void coletar(Component raiz, Class<T> tipo, List<T> encontrados) {
        if (tipo.isInstance(raiz)) encontrados.add(tipo.cast(raiz));
        if (raiz instanceof Container container) {
            for (Component filho : container.getComponents()) coletar(filho, tipo, encontrados);
        }
    }

    private static Component telaVisivel(Container area) {
        for (Component filho : area.getComponents()) if (filho.isVisible()) return filho;
        throw new AssertionError("Nenhuma tela ativa.");
    }

    private static void layoutTree(Component componente) {
        if (componente instanceof Container container) {
            container.doLayout();
            for (Component filho : container.getComponents()) layoutTree(filho);
        }
    }
}
