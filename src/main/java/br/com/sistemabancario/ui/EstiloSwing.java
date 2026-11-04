package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Insets;
import java.awt.BasicStroke;
import javax.swing.border.AbstractBorder;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.JScrollPane;

final class EstiloSwing {
    static final Color MARINHO = new Color(65, 20, 30);
    static final Color AZUL = new Color(177, 35, 55);
    static final Color AZUL_ATIVO = new Color(139, 27, 43);
    static final Color AZUL_CLARO = new Color(250, 235, 238);
    static final Color FUNDO = new Color(246, 245, 246);
    static final Color TEXTO = new Color(43, 37, 39);
    static final Color SECUNDARIO = new Color(112, 101, 105);
    static final Color BORDA = new Color(230, 222, 224);
    static final Color LINHA_TABELA = new Color(241, 235, 237);
    static final Color SUCESSO = new Color(36, 125, 82);
    static final Color ERRO = new Color(174, 48, 59);
    static final Color AVISO = new Color(153, 96, 24);
    static final Color FUNDO_INFO = new Color(253, 246, 247);
    static final Font FONTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 24);
    static final Font FONTE_CORPO = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

    private EstiloSwing() { }

    static String moeda(java.math.BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    static void campo(JTextField campo) {
        campo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        campo.setForeground(TEXTO);
        campo.setOpaque(false);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new BordaCampo(BORDA), BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        campo.setPreferredSize(new Dimension(260, 48));
    }

    static void componente(JComponent componente) {
        componente.setFont(FONTE_CORPO);
        componente.setForeground(TEXTO);
    }

    static void tabela(JTable tabela) {
        tabela.setFont(FONTE_CORPO);
        tabela.setForeground(TEXTO);
        tabela.setBackground(Color.WHITE);
        tabela.setGridColor(LINHA_TABELA);
        tabela.setShowVerticalLines(false);
        tabela.setRowHeight(42);
        tabela.setSelectionBackground(AZUL_CLARO);
        tabela.setSelectionForeground(TEXTO);
        tabela.setFillsViewportHeight(true);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabela.getTableHeader().setForeground(TEXTO);
        tabela.getTableHeader().setBackground(new Color(248, 250, 253));
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 42));
        tabela.getTableHeader().setReorderingAllowed(false);
    }

    static void cartao(JPanel painel) {
        painel.setBackground(Color.WHITE);
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createCompoundBorder(
                new BordaArredondada(Color.WHITE, BORDA, 22, 18),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    static JLabel rotuloCampo(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        rotulo.setForeground(TEXTO);
        return rotulo;
    }

    static JPanel cabecalhoTela(String titulo, String descricao) {
        JPanel textos = new JPanel(new java.awt.GridLayout(0, 1, 0, 5));
        textos.setOpaque(false);
        JLabel nome = new JLabel(titulo);
        nome.setFont(FONTE_TITULO);
        nome.setForeground(TEXTO);
        JLabel explicacao = new JLabel("<html><body style='width:330px'>" + descricao + "</body></html>");
        explicacao.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        explicacao.setForeground(SECUNDARIO);
        textos.add(nome);
        textos.add(explicacao);
        return textos;
    }

    static JButton botao(String texto, boolean principal) {
        JButton botao = new BotaoArredondado(texto);
        botao.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        botao.setFocusPainted(false);
        botao.setOpaque(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setRolloverEnabled(true);
        botao.setBackground(principal ? AZUL : Color.WHITE);
        botao.setForeground(principal ? Color.WHITE : TEXTO);
        botao.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        return botao;
    }

    static JButton botaoFinanceiro(String texto, Color cor) {
        JButton botao = botao(texto, true);
        botao.setBackground(cor);
        return botao;
    }

    static void botaoNavegacao(AbstractButton botao, boolean selecionado) {
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        botao.setForeground(selecionado ? AZUL_ATIVO : SECUNDARIO);
        botao.setBackground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(false);
        botao.setVerticalTextPosition(AbstractButton.BOTTOM);
        botao.setHorizontalTextPosition(AbstractButton.CENTER);
        botao.setIcon(new IconeNavegacao(botao.getActionCommand(),
                selecionado ? AZUL_ATIVO : SECUNDARIO));
        botao.setIconTextGap(4);
        botao.setBorder(BorderFactory.createEmptyBorder(5, 1, 4, 1));
    }

    static void contas(JComboBox<Conta> combo) {
        componente(combo);
        combo.setPreferredSize(new Dimension(340, 48));
        combo.setMinimumSize(new Dimension(140, 44));
        ListCellRenderer<? super Conta> base = combo.getRenderer();
        combo.setRenderer((lista, conta, indice, selecionado, foco) -> {
            Component componente = base.getListCellRendererComponent(lista, conta, indice, selecionado, foco);
            if (conta != null && componente instanceof javax.swing.JLabel rotulo) {
                rotulo.setText(conta.getTitular().getNome() + " — " + conta.getIdentificadorCurto()
                        + " — " + moeda(conta.getSaldo()));
                rotulo.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            }
            return componente;
        });
    }

    static JScrollPane rolagemNatural(JScrollPane rolagem) {
        rolagem.setWheelScrollingEnabled(true);
        rolagem.getVerticalScrollBar().setUnitIncrement(20);
        rolagem.getHorizontalScrollBar().setUnitIncrement(20);
        return rolagem;
    }

    private static final class BordaArredondada extends AbstractBorder {
        private final Color fundo;
        private final Color borda;
        private final int raio;
        private final int margem;

        private BordaArredondada(Color fundo, Color borda, int raio, int margem) {
            this.fundo = fundo;
            this.borda = borda;
            this.raio = raio;
            this.margem = margem;
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(margem, margem, margem, margem);
        }

        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(margem, margem, margem, margem);
            return insets;
        }

        @Override public void paintBorder(Component c, Graphics grafico, int x, int y, int largura, int altura) {
            Graphics2D g = (Graphics2D) grafico.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fundo);
            g.fillRoundRect(x, y, largura - 1, altura - 1, raio, raio);
            g.setColor(borda);
            g.drawRoundRect(x, y, largura - 1, altura - 1, raio, raio);
            g.dispose();
        }
    }

    private static final class BotaoArredondado extends JButton {
        private BotaoArredondado(String texto) { super(texto); }

        @Override protected void paintComponent(Graphics grafico) {
            Graphics2D g = (Graphics2D) grafico.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fundo = !isEnabled() ? new Color(232, 228, 229)
                    : getModel().isPressed() && getModel().isArmed() ? getBackground().darker()
                    : getModel().isRollover() && getBackground().equals(Color.WHITE)
                    ? new Color(249, 242, 243)
                    : getModel().isRollover() ? getBackground().brighter() : getBackground();
            g.setColor(fundo);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            if (isFocusOwner()) {
                g.setColor(AZUL_ATIVO);
                g.setStroke(new BasicStroke(1.4f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            }
            g.dispose();
            super.paintComponent(grafico);
        }
    }

    private static final class BordaCampo extends AbstractBorder {
        private final Color cor;
        private BordaCampo(Color cor) { this.cor = cor; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(1, 1, 1, 1);
            return insets;
        }
        @Override public void paintBorder(Component c, Graphics grafico, int x, int y, int largura, int altura) {
            Graphics2D g = (Graphics2D) grafico.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c.hasFocus() ? AZUL : cor);
            g.drawRoundRect(x, y, largura - 1, altura - 1, 12, 12);
            g.dispose();
        }
    }
}
