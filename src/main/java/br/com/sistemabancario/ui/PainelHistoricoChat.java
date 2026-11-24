package br.com.sistemabancario.ui;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.text.View;

/** Histórico rolável com colunas de mensagem LTR e altura determinada pelo conteúdo. */
final class PainelHistoricoChat extends JPanel implements Scrollable {
    private static final int ESPACAMENTO_MENSAGENS = 22;
    private static final int LARGURA_MAXIMA_MENSAGEM = 680;
    private static final int MARGEM_HORIZONTAL = 48;
    private final List<ItemMensagem> mensagens = new ArrayList<>();

    PainelHistoricoChat() {
        super(new LayoutHistorico());
        setBackground(EstiloSwing.FUNDO);
        setOpaque(true);
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent evento) {
                ajustarLarguras();
            }
        });
    }

    void adicionarMensagem(String autor, String conteudo, boolean usuario) {
        ItemMensagem mensagem = new ItemMensagem(autor, conteudo, usuario);
        mensagens.add(mensagem);
        JPanel linha = new JPanel(new FlowLayout(
                usuario ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        linha.add(mensagem, usuario ? BorderLayout.EAST : BorderLayout.WEST);
        add(linha);
        ajustarLarguras();
        revalidate();
        repaint();
    }

    void ajustarLarguras() {
        int larguraDisponivel = getWidth() - MARGEM_HORIZONTAL;
        int largura = Math.max(48, Math.min(LARGURA_MAXIMA_MENSAGEM,
                (int) (Math.max(64, larguraDisponivel) * 0.74)));
        mensagens.forEach(mensagem -> mensagem.definirLargura(largura));
        revalidate();
        repaint();
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
        return 24;
    }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? Math.max(visible.height - 24, 24) : visible.width;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }

    private static final class ItemMensagem extends JPanel {
        private static final int MARGEM_TEXTO = 4;
        private final JTextArea texto = new JTextArea();
        private final String conteudo;

        private ItemMensagem(String autor, String conteudo, boolean usuario) {
            super();
            this.conteudo = conteudo;
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
            setOpaque(false);
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            JLabel remetente = new JLabel(autor);
            remetente.setFont(EstiloSwing.FONTE_MICRO);
            remetente.setForeground(usuario ? EstiloSwing.ROSA_ATIVO : EstiloSwing.SECUNDARIO);
            remetente.setAlignmentX(usuario ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);

            texto.setFont(EstiloSwing.FONTE_CORPO);
            texto.setForeground(EstiloSwing.TEXTO);
            texto.setLineWrap(true);
            texto.setWrapStyleWord(true);
            texto.setEditable(false);
            texto.setFocusable(false);
            texto.setOpaque(false);
            texto.setBorder(BorderFactory.createEmptyBorder());
            texto.setAlignmentX(usuario ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
            texto.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            add(remetente);
            add(javax.swing.Box.createVerticalStrut(5));
            add(texto);
        }

        private void definirLargura(int larguraMaxima) {
            int larguraNatural = larguraNaturalDoConteudo();
            int largura = Math.max(48, Math.min(larguraMaxima, larguraNatural));
            int larguraTexto = Math.max(24, largura - MARGEM_TEXTO);
            texto.setText(conteudo);
            texto.setPreferredSize(null);
            texto.setSize(new Dimension(larguraTexto, Short.MAX_VALUE));
            View visualizacao = texto.getUI().getRootView(texto);
            visualizacao.setSize(larguraTexto, Short.MAX_VALUE);
            Insets margensTexto = texto.getInsets();
            int alturaTexto = (int) Math.ceil(visualizacao.getPreferredSpan(View.Y_AXIS))
                    + margensTexto.top + margensTexto.bottom;
            texto.setPreferredSize(new Dimension(larguraTexto, alturaTexto));
            texto.setSize(larguraTexto, alturaTexto);
            Dimension tamanhoMensagem = new Dimension(largura, alturaTexto + 20);
            setPreferredSize(tamanhoMensagem);
            setMaximumSize(tamanhoMensagem);
        }

        private int larguraNaturalDoConteudo() {
            var metricaTexto = texto.getFontMetrics(texto.getFont());
            int larguraTexto = 0;
            for (String linha : conteudo.split("\\R", -1)) {
                larguraTexto = Math.max(larguraTexto, metricaTexto.stringWidth(linha));
            }
            JLabel remetente = (JLabel) getComponent(0);
            int larguraRemetente = remetente.getFontMetrics(remetente.getFont()).stringWidth(remetente.getText());
            return Math.max(larguraTexto, larguraRemetente) + MARGEM_TEXTO;
        }
    }

    private static final class LayoutHistorico implements LayoutManager2 {
        @Override public void addLayoutComponent(Component componente, Object restricoes) { }
        @Override public void addLayoutComponent(String nome, Component componente) { }
        @Override public void removeLayoutComponent(Component componente) { }
        @Override public float getLayoutAlignmentX(Container alvo) { return 0.0f; }
        @Override public float getLayoutAlignmentY(Container alvo) { return 0.0f; }
        @Override public void invalidateLayout(Container alvo) { }

        @Override public Dimension preferredLayoutSize(Container alvo) {
            synchronized (alvo.getTreeLock()) {
                Insets insets = alvo.getInsets();
                int largura = 0;
                int altura = 0;
                int quantidade = alvo.getComponentCount();
                for (Component componente : alvo.getComponents()) {
                    Dimension preferido = componente.getPreferredSize();
                    largura = Math.max(largura, preferido.width);
                    altura += preferido.height;
                }
                if (quantidade > 1) altura += ESPACAMENTO_MENSAGENS * (quantidade - 1);
                return new Dimension(largura + insets.left + insets.right,
                        altura + insets.top + insets.bottom);
            }
        }

        @Override public Dimension minimumLayoutSize(Container alvo) {
            synchronized (alvo.getTreeLock()) {
                Insets insets = alvo.getInsets();
                int largura = 0;
                int altura = 0;
                int quantidade = alvo.getComponentCount();
                for (Component componente : alvo.getComponents()) {
                    Dimension minimo = componente.getMinimumSize();
                    largura = Math.max(largura, minimo.width);
                    altura += minimo.height;
                }
                if (quantidade > 1) altura += ESPACAMENTO_MENSAGENS * (quantidade - 1);
                return new Dimension(largura + insets.left + insets.right,
                        altura + insets.top + insets.bottom);
            }
        }

        @Override public Dimension maximumLayoutSize(Container alvo) {
            Dimension preferido = preferredLayoutSize(alvo);
            return new Dimension(Integer.MAX_VALUE, preferido.height);
        }

        @Override public void layoutContainer(Container alvo) {
            synchronized (alvo.getTreeLock()) {
                Insets insets = alvo.getInsets();
                int largura = Math.max(0, alvo.getWidth() - insets.left - insets.right);
                int y = insets.top;
                for (Component componente : alvo.getComponents()) {
                    int altura = componente.getPreferredSize().height;
                    componente.setBounds(insets.left, y, largura, altura);
                    y += altura + ESPACAMENTO_MENSAGENS;
                }
            }
        }
    }
}
