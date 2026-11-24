package br.com.sistemabancario.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelChatTest {
    private static final int ESPACAMENTO_MENSAGENS = 22;

    @Test
    void historicoEscuroSemCartaoOcupaCentroEEntradaFicaAbaixo() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelChat chat = new PainelChat(mensagem -> "Resposta de teste");
            chat.setSize(390, 680);
            layoutTree(chat);
            JScrollPane conversa = encontrarTodos(chat, JScrollPane.class).stream()
                    .filter(painel -> painel.getViewport().getView() instanceof JPanel)
                    .findFirst().orElseThrow();
            assertTrue(conversa.getPreferredSize().height > 0);
            var insets = conversa.getBorder().getBorderInsets(conversa);
            assertEquals(0, insets.top + insets.bottom + insets.left + insets.right);
            assertTrue(conversa.getVerticalScrollBarPolicy() == JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            assertEquals(EstiloSwing.FUNDO, conversa.getViewport().getView().getBackground());
            assertTrue(conversa.getHeight() > 0);
            JPanel historico = (JPanel) conversa.getViewport().getView();
            assertFalse(historico.getLayout() instanceof javax.swing.BoxLayout,
                    "A pilha não deve distribuir altura excedente entre linhas.");
            assertTrue(historico.getPreferredSize().height < conversa.getHeight(),
                    "Com poucas mensagens, o histórico deve acompanhar o conteúdo e deixar o espaço livre embaixo.");
            assertTrue(encontrarTodos(chat, JTextField.class).stream().anyMatch(campo -> campo.getWidth() > 0));
            JButton enviar = encontrarTodos(chat, JButton.class).stream()
                    .filter(botao -> "Enviar".equals(botao.getText())).findFirst().orElseThrow();
            assertTrue(enviar.getWidth() > 0);
            assertTrue(chat.getLayout() instanceof java.awt.BorderLayout,
                    "A composição deve reservar o centro ao histórico e o rodapé à entrada.");
            assertTrue(encontrarTodos(chat, JLabel.class).stream()
                    .noneMatch(rotulo -> "Conversa".equals(rotulo.getText())
                            || rotulo.getText().contains("Assistente informativo")));
        });
    }

    @Test
    void linhasEmpilhamPelasAlturasNaturaisComEspacamentoConstante() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelChat chat = new PainelChat(mensagem -> "Resposta de teste");
            adicionarMensagem(chat, "Você", "pergunta longa ".repeat(36));
            adicionarMensagem(chat, "Assistente", "Resposta curta.");
            chat.setSize(1080, 760);
            layoutTree(chat);
            chat.setSize(1010, 730);
            layoutTree(chat);

            JScrollPane rolagem = encontrarTodos(chat, JScrollPane.class).getFirst();
            PainelHistoricoChat historico = (PainelHistoricoChat) rolagem.getViewport().getView();
            int larguraView = 900;
            int alturaViewport = 600;
            rolagem.setSize(larguraView, alturaViewport);
            rolagem.doLayout();
            rolagem.getViewport().setSize(larguraView, alturaViewport);
            rolagem.getViewport().doLayout();
            historico.setSize(larguraView, alturaViewport);
            historico.ajustarLarguras();
            historico.revalidate();
            historico.doLayout();
            assertTrue(historico.getPreferredSize().height < alturaViewport,
                    "As três mensagens devem deixar espaço livre: conteúdo="
                            + historico.getPreferredSize().height + ", viewport=" + alturaViewport
                            + ", alturas=" + java.util.Arrays.toString(java.util.Arrays.stream(
                                    historico.getComponents()).mapToInt(c -> c.getPreferredSize().height).toArray()));

            assertEquals(alturaViewport, historico.getHeight(),
                    "O caso deve reproduzir uma view que ocupa exatamente a altura da viewport.");
            assertEquals(3, historico.getComponentCount());
            assertTrue(historico.getComponent(0).getHeight() < 150,
                    "A largura final da viewport deve recalcular a altura da mensagem inicial.");
            assertTrue(historico.getComponent(1).getHeight() > historico.getComponent(0).getHeight(),
                    "A segunda mensagem tem conteúdo maior e deve ocupar mais linhas.");
            int alturaEsperada = historico.getInsets().top + historico.getInsets().bottom;
            for (int indice = 0; indice < historico.getComponentCount(); indice++) {
                Component linha = historico.getComponent(indice);
                assertEquals(linha.getPreferredSize().height, linha.getHeight(),
                        "A linha deve usar apenas a altura natural do conteúdo.");
                if (indice > 0) {
                    Component anterior = historico.getComponent(indice - 1);
                    assertEquals(anterior.getY() + anterior.getHeight() + ESPACAMENTO_MENSAGENS,
                            linha.getY(), "Cada mensagem deve seguir a anterior com intervalo constante.");
                    alturaEsperada += ESPACAMENTO_MENSAGENS;
                }
                alturaEsperada += linha.getPreferredSize().height;
            }
            assertEquals(alturaEsperada, historico.getPreferredSize().height,
                    "A altura preferencial deve ser a soma das mensagens e dos intervalos.");
        });
    }

    @Test
    void textoDoUsuarioPreservaPontuacaoEmDirecaoLtr() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelChat chat = new PainelChat(mensagem -> "Resposta de teste");
            List<String> entradas = List.of(
                    "o que é o pix?",
                    "Olá! Tudo bem?",
                    "Você usa Pix (instantâneo)?",
                    "Acentuação, ação e informação!",
                    "Confirme (por favor), agora?!");
            entradas.forEach(texto -> adicionarMensagem(chat, "Você", texto));

            List<JTextArea> textos = encontrarTodos(chat, JTextArea.class);
            assertEquals(entradas.size() + 1, textos.size());
            for (int indice = 0; indice < entradas.size(); indice++) {
                assertEquals(entradas.get(indice), textos.get(indice + 1).getText());
                assertEquals(java.awt.ComponentOrientation.LEFT_TO_RIGHT,
                        textos.get(indice + 1).getComponentOrientation(),
                        "Pontuação em português deve permanecer na ordem original.");
            }
            JTextField entrada = encontrarTodos(chat, JTextField.class).getFirst();
            assertEquals(java.awt.ComponentOrientation.LEFT_TO_RIGHT, entrada.getComponentOrientation());
        });
    }

    @Test
    void linhasAlinhamAssistenteAEsquerdaEUsuarioADireitaComLarguraNatural() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelChat chat = new PainelChat(mensagem -> "Resposta de teste");
            adicionarMensagem(chat, "Assistente", "Resposta curta.");
            adicionarMensagem(chat, "Você", "Mensagem curta?");
            adicionarMensagem(chat, "Você", "Uma mensagem longa para verificar que o balão respeita "
                    + "o limite de largura e continua alinhado à direita. ".repeat(18));
            chat.setSize(1280, 820);
            layoutTree(chat);
            historico(chat).ajustarLarguras();
            layoutTree(chat);

            PainelHistoricoChat historico = historico(chat);
            Component linhaAssistente = historico.getComponent(1);
            Component linhaUsuarioCurta = historico.getComponent(2);
            Component linhaUsuarioLonga = historico.getComponent(3);
            linhaAssistente.doLayout();
            linhaUsuarioCurta.doLayout();
            linhaUsuarioLonga.doLayout();

            Component mensagemAssistente = ((Container) linhaAssistente).getComponent(0);
            Component mensagemUsuarioCurta = ((Container) linhaUsuarioCurta).getComponent(0);
            Component mensagemUsuarioLonga = ((Container) linhaUsuarioLonga).getComponent(0);
            assertEquals(0, mensagemAssistente.getX(), "Mensagem do assistente começa na margem esquerda.");
            assertEquals(linhaUsuarioCurta.getWidth(), mensagemUsuarioCurta.getX() + mensagemUsuarioCurta.getWidth(),
                    "Mensagem do usuário termina na margem direita da linha.");
            assertEquals(linhaUsuarioLonga.getWidth(), mensagemUsuarioLonga.getX() + mensagemUsuarioLonga.getWidth(),
                    "Mensagem longa do usuário também termina na margem direita.");
            assertTrue(mensagemUsuarioCurta.getWidth() < mensagemUsuarioLonga.getWidth(),
                    "A mensagem curta deve usar largura ajustada ao conteúdo.");
            assertTrue(mensagemUsuarioLonga.getWidth() <= 680,
                    "Mensagens longas devem respeitar a largura máxima.");
            JTextArea textoUsuario = ((Container) mensagemUsuarioCurta).getComponents().length > 2
                    ? encontrarTodos(mensagemUsuarioCurta, JTextArea.class).getFirst() : null;
            assertNotNull(textoUsuario);
            assertEquals(java.awt.ComponentOrientation.LEFT_TO_RIGHT, textoUsuario.getComponentOrientation());
        });
    }

    @Test
    void envioEnterExecutaForaDaEdtMostraEstadoEAceitaCancelamento() throws Exception {
        CountDownLatch requisicaoIniciada = new CountDownLatch(1);
        AtomicReference<Thread> threadRequisicao = new AtomicReference<>();
        AtomicReference<Thread> threadEdt = new AtomicReference<>();
        PainelChat chat = new PainelChat(mensagem -> {
            threadRequisicao.set(Thread.currentThread());
            requisicaoIniciada.countDown();
            new CountDownLatch(1).await();
            return "Resposta que deve ser cancelada";
        });

        SwingUtilities.invokeAndWait(() -> {
            threadEdt.set(Thread.currentThread());
            JTextField entrada = encontrarTodos(chat, JTextField.class).getFirst();
            entrada.setText("O que é o Pix?");
            entrada.postActionEvent();
        });

        assertTrue(requisicaoIniciada.await(3, TimeUnit.SECONDS));
        assertNotSame(threadEdt.get(), threadRequisicao.get(), "A chamada HTTP não pode executar na EDT.");
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(encontrarTodos(chat, JTextField.class).getFirst().isEnabled());
            JButton botao = encontrarTodos(chat, JButton.class).stream()
                    .filter(item -> "Cancelar".equals(item.getText())).findFirst().orElseThrow();
            assertTrue(botao.isEnabled());
            botao.doClick();
        });

        assertTrue(aguardarEstado(chat, "Geração cancelada.", "Enviar"));
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(encontrarTodos(chat, JTextField.class).getFirst().isEnabled());
            assertTrue(encontrarTodos(chat, JButton.class).stream()
                    .anyMatch(item -> "Enviar".equals(item.getText()) && item.isEnabled()));
        });
    }

    @Test
    void respostaAssincronaRestauraControlesEDeixaHistoricoNoFinal() throws Exception {
        CountDownLatch requisicaoIniciada = new CountDownLatch(1);
        AtomicReference<Thread> threadRequisicao = new AtomicReference<>();
        AtomicReference<Thread> threadEdt = new AtomicReference<>();
        PainelChat chat = new PainelChat(mensagem -> {
            threadRequisicao.set(Thread.currentThread());
            requisicaoIniciada.countDown();
            return "Resposta válida.";
        });

        SwingUtilities.invokeAndWait(() -> {
            PainelHistoricoChat historico = historico(chat);
            for (int indice = 0; indice < 12; indice++) {
                historico.adicionarMensagem("Assistente", ("Linha de histórico longa %d. ").formatted(indice).repeat(8), false);
            }
            chat.setSize(430, 320);
            layoutTree(chat);
            threadEdt.set(Thread.currentThread());
            JTextField entrada = encontrarTodos(chat, JTextField.class).getFirst();
            entrada.setText("O que é o Pix?");
            entrada.postActionEvent();
        });

        assertTrue(requisicaoIniciada.await(3, TimeUnit.SECONDS));
        assertNotSame(threadEdt.get(), threadRequisicao.get(), "A geração deve ocorrer fora da EDT.");
        assertTrue(aguardarEstado(chat, "Resposta pronta.", "Enviar"));
        SwingUtilities.invokeAndWait(() -> layoutTree(chat));
        assertTrue(aguardarRolagemNoFinal(chat),
                "A rolagem deve acompanhar a validação do novo tamanho da view: " + estadoRolagem(chat));
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(encontrarTodos(chat, JTextField.class).getFirst().isEnabled());
            JScrollPane rolagem = encontrarTodos(chat, JScrollPane.class).getFirst();
            assertEquals(rolagem.getVerticalScrollBar().getMaximum(),
                    rolagem.getVerticalScrollBar().getValue() + rolagem.getVerticalScrollBar().getVisibleAmount(),
                    "A conclusão da resposta deve rolar até a mensagem mais recente.");
            List<JTextArea> textos = encontrarTodos(chat, JTextArea.class);
            assertTrue(textos.stream().anyMatch(texto -> "Resposta válida.".equals(texto.getText())));
        });
    }

    @Test
    void erroDeComunicacaoEApresentadoEControlesSaoRestaurados() throws Exception {
        PainelChat chat = new PainelChat(mensagem -> {
            throw new IllegalStateException("Conexão recusada pelo Qwen");
        });
        SwingUtilities.invokeAndWait(() -> {
            JTextField entrada = encontrarTodos(chat, JTextField.class).getFirst();
            entrada.setText("Olá?");
            entrada.postActionEvent();
        });

        assertTrue(aguardarEstado(chat, "Erro no chat: Conexão recusada pelo Qwen", "Enviar"));
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(encontrarTodos(chat, JTextField.class).getFirst().isEnabled());
            assertTrue(encontrarTodos(chat, JButton.class).stream()
                    .anyMatch(botao -> "Enviar".equals(botao.getText()) && botao.isEnabled()));
        });
    }

    private static boolean aguardarEstado(PainelChat chat, String estadoEsperado, String botaoEsperado)
            throws Exception {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < limite) {
            AtomicReference<Boolean> pronto = new AtomicReference<>(false);
            SwingUtilities.invokeAndWait(() -> pronto.set(
                    encontrarTodos(chat, JLabel.class).stream().anyMatch(rotulo -> estadoEsperado.equals(rotulo.getText()))
                    && encontrarTodos(chat, JButton.class).stream().anyMatch(botao -> botaoEsperado.equals(botao.getText()))));
            if (pronto.get()) return true;
            Thread.sleep(15);
        }
        return false;
    }

    private static boolean aguardarRolagemNoFinal(PainelChat chat) throws Exception {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < limite) {
            AtomicReference<Boolean> noFinal = new AtomicReference<>(false);
            SwingUtilities.invokeAndWait(() -> {
                JScrollPane rolagem = encontrarTodos(chat, JScrollPane.class).getFirst();
                noFinal.set(rolagem.getVerticalScrollBar().getValue()
                        + rolagem.getVerticalScrollBar().getVisibleAmount()
                        == rolagem.getVerticalScrollBar().getMaximum());
            });
            if (noFinal.get()) return true;
            Thread.sleep(15);
        }
        return false;
    }

    private static String estadoRolagem(PainelChat chat) throws Exception {
        AtomicReference<String> estado = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            JScrollPane rolagem = encontrarTodos(chat, JScrollPane.class).getFirst();
            var barra = rolagem.getVerticalScrollBar();
            estado.set("valor=" + barra.getValue() + ", extent=" + barra.getVisibleAmount()
                    + ", max=" + barra.getMaximum() + ", view=" + rolagem.getViewport().getView().getHeight());
        });
        return estado.get();
    }

    private static void adicionarMensagem(PainelChat chat, String autor, String texto) {
        historico(chat).adicionarMensagem(autor, texto, "Você".equals(autor));
    }

    private static PainelHistoricoChat historico(PainelChat chat) {
        return (PainelHistoricoChat) encontrarTodos(chat, JScrollPane.class).getFirst().getViewport().getView();
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
