package br.com.sistemabancario.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelAreaRolavelTest {
    @Test
    void telasCabemNaLarguraDaViewportEConteudoPermiteRolagemVertical() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CardLayout layout = new CardLayout();
            PainelAreaRolavel area = new PainelAreaRolavel(layout);
            Component inicio = new PainelInicio(destino -> { });
            Component contas = new PainelContas(() -> { }, (destino, selecionada) -> { }, selecionada -> { });
            Component operacoes = new PainelOperacoes(
                    new PainelOperacao(PainelOperacao.Tipo.DEPOSITO, (conta, valor) -> { }, true),
                    new PainelOperacao(PainelOperacao.Tipo.SAQUE, (conta, valor) -> { }, true),
                    new PainelTransferencia((origem, destino, valor) -> { }, true));
            Component extrato = new PainelExtrato(selecionada -> { }, (selecionada, arquivo) -> { });
            Component chat = new PainelChat(new ClienteChatSimulado());
            JanelaPrincipal.registrarPaginas(area, inicio, contas, operacoes, extrato, chat);

            javax.swing.JPanel conteudoPrincipal = new javax.swing.JPanel(new java.awt.BorderLayout());
            JScrollPane rolagem = JanelaPrincipal.instalarAreaRolavel(conteudoPrincipal, area);
            assertSame(rolagem, ((java.awt.BorderLayout) conteudoPrincipal.getLayout())
                    .getLayoutComponent(java.awt.BorderLayout.CENTER));
            assertSame(area, rolagem.getViewport().getView());
            rolagem.setSize(398, 680);
            rolagem.doLayout();
            rolagem.getViewport().doLayout();
            area.doLayout();
            int larguraViewport = rolagem.getViewport().getExtentSize().width;
            for (String tela : List.of("inicio", "contas", "operacoes", "extrato", "chat")) {
                area.mostrar(tela);
                assertEquals("chat".equals(tela), area.getScrollableTracksViewportHeight(),
                        "Somente o chat acompanha a altura da viewport externa.");
                area.doLayout();
                Component ativa = telaVisivel(area);
                assertEquals(tela, area.telaAtual());
                assertSame(ativa, area.componente(tela));
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
    void chatReposicionaEntradaERolagemAoRedimensionarViewport() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelAreaRolavel area = new PainelAreaRolavel(new CardLayout());
            PainelChat chat = new PainelChat(new ClienteChatSimulado());
            area.registrar("chat", chat);
            area.mostrar("chat");

            javax.swing.JPanel hospedeiro = new javax.swing.JPanel(new java.awt.BorderLayout());
            JScrollPane externa = JanelaPrincipal.instalarAreaRolavel(hospedeiro, area);
            externa.setSize(400, 620);
            layoutExterna(externa, area, chat);
            int alturaInicial = chat.getHeight();
            JScrollPane mensagens = encontrarTodos(chat, JScrollPane.class).getFirst();
            JTextField entrada = encontrarTodos(chat, JTextField.class).getFirst();
            JButton enviar = encontrarTodos(chat, JButton.class).stream()
                    .filter(botao -> "Enviar".equals(botao.getText())).findFirst().orElseThrow();
            assertEquals(externa.getViewport().getExtentSize().height, area.getHeight());
            assertEquals(area.getHeight(), chat.getHeight());
            assertEquals(entrada.getY(), enviar.getY(), "Campo e botão permanecem na mesma linha do rodapé.");

            externa.setSize(1280, 900);
            externa.revalidate();
            layoutExterna(externa, area, chat);

            assertTrue(chat.getHeight() > alturaInicial, "O painel deve crescer junto com a janela.");
            assertEquals(externa.getViewport().getExtentSize().height, area.getHeight());
            assertEquals(area.getHeight(), chat.getHeight());
            assertTrue(mensagens.getHeight() > 0);
            assertTrue(mensagens.getHeight() > 400, "O histórico deve receber a altura adicional disponível.");
            assertEquals(entrada.getY(), enviar.getY(), "Campo e botão devem continuar alinhados após maximizar.");
            assertTrue(entrada.getY() + entrada.getHeight() <= chat.getHeight());
            assertTrue(enviar.getX() + enviar.getWidth() < chat.getWidth(),
                    "O botão mantém a margem lateral do rodapé.");
        });
    }

    @Test
    void rejeitaRotasNaoRegistradasEIdentificadoresDuplicados() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelAreaRolavel area = new PainelAreaRolavel(new CardLayout());
            area.registrar("inicio", new PainelInicio(destino -> { }));
            assertThrows(IllegalArgumentException.class, () -> area.mostrar("inexistente"));
            assertThrows(IllegalArgumentException.class,
                    () -> area.registrar("inicio", new javax.swing.JPanel()));
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

    private static void layoutExterna(JScrollPane externa, PainelAreaRolavel area, PainelChat chat) {
        externa.doLayout();
        externa.getViewport().doLayout();
        area.doLayout();
        layoutTree(chat);
    }
}
