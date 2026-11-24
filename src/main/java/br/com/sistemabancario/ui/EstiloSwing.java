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
import java.math.BigDecimal;
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
import javax.swing.JPasswordField;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.ListCellRenderer;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

final class EstiloSwing {
    static final Color ROSA_AVERMELHADO = new Color(235, 79, 98);
    static final Color VINHO = new Color(194, 43, 62);
    static final Color ROSA_ATIVO = new Color(238, 77, 96);
    static final Color ROSA_TEXTO = new Color(145, 31, 49);
    static final Color VINHO_ESCURO = new Color(75, 34, 42);
    static final Color FUNDO = new Color(24, 21, 23);
    static final Color SUPERFICIE = new Color(37, 32, 35);
    static final Color SUPERFICIE_CARTAO = new Color(45, 37, 41);
    static final Color SUPERFICIE_ALTERNADA = new Color(41, 35, 38);
    static final Color SUPERFICIE_CABECALHO = new Color(48, 40, 44);
    static final Color SUPERFICIE_HOVER = new Color(54, 43, 48);
    static final Color SUPERFICIE_CLARA = new Color(244, 237, 239);
    static final Color SUPERFICIE_CLARA_ALTERNADA = new Color(236, 226, 229);
    static final Color SUPERFICIE_CLARA_HOVER = new Color(250, 243, 245);
    static final Color FUNDO_CAMPO_CLARO = new Color(251, 247, 248);
    static final Color TEXTO_ESCURO = new Color(55, 31, 39);
    static final Color SECUNDARIO_CLARO = new Color(112, 88, 96);
    static final Color BORDA_CLARA = new Color(213, 194, 200);
    static final Color DESTAQUE_CLARO = new Color(249, 226, 231);
    static final Color FUNDO_DESATIVADO = new Color(58, 51, 54);
    static final Color TEXTO = new Color(244, 237, 240);
    static final Color SECUNDARIO = new Color(181, 168, 173);
    static final Color BORDA = new Color(74, 60, 65);
    static final Color BORDA_SUAVE = new Color(65, 53, 58);
    static final Color SOMBRA_CARTAO = new Color(12, 5, 8, 30);
    static final Color SOMBRA_CARTAO_SUAVE = new Color(12, 5, 8, 18);
    static final Color LINHA_TABELA = new Color(57, 47, 51);
    static final Color ICONE_NEUTRO = new Color(226, 205, 209);
    static final Color SUCESSO = new Color(82, 196, 136);
    static final Color SUCESSO_TEXTO = new Color(32, 111, 72);
    static final Color FUNDO_SUCESSO = new Color(32, 56, 46);
    static final Color ERRO = new Color(239, 91, 109);
    static final Color ERRO_TEXTO = new Color(151, 37, 53);
    static final Color FUNDO_ERRO = new Color(65, 35, 41);
    static final Color AVISO = new Color(236, 174, 88);
    static final Color FUNDO_AVISO = new Color(61, 48, 32);
    static final Color FUNDO_INFO = new Color(48, 30, 35);
    static final Color FUNDO_CHAT_USUARIO = new Color(75, 34, 42);
    static final Color FUNDO_CHAT_ASSISTENTE = new Color(37, 32, 35);
    static final Color BORDA_CHAT_USUARIO = new Color(128, 54, 67);
    static final Color COR_INDICADOR = new Color(207, 112, 126);
    static final Font FONTE_MICRO = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    static final Font FONTE_AUXILIAR = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    static final Font FONTE_CAMPO = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    static final Font FONTE_SECAO = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    static final Font FONTE_TITULO_CARTAO = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    static final Font FONTE_MARCA_LOGIN = new Font(Font.SANS_SERIF, Font.BOLD, 27);
    static final Font FONTE_MONOGRAMA = new Font(Font.SANS_SERIF, Font.BOLD, 25);
    static final Font FONTE_CHAMADA = new Font(Font.SANS_SERIF, Font.BOLD, 30);
    static final Font FONTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 24);
    static final Font FONTE_VALOR = new Font(Font.SANS_SERIF, Font.BOLD, 25);
    static final Font FONTE_CORPO = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    static final Dimension TAMANHO_CAMPO = new Dimension(260, 42);
    static final Dimension TAMANHO_BOTAO = new Dimension(0, 38);
    static final Dimension TAMANHO_BOTAO_COMPACTO = new Dimension(0, 32);
    static final int LARGURA_ACESSO_AMPLA = 880;
    static final int LARGURA_NAVEGACAO_LATERAL = 900;
    static final int LARGURA_OPERACOES_TRES_COLUNAS = 1160;
    static final int LARGURA_OPERACOES_DUAS_COLUNAS = 650;
    static final int LARGURA_ATALHOS_AMPLA = 760;
    static final int LARGURA_EXTRATO_HORIZONTAL = 700;

    private EstiloSwing() { }

    static String moeda(java.math.BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    static void campo(JTextField campo) {
        campo.setFont(FONTE_CAMPO);
        campo.setForeground(TEXTO);
        campo.setCaretColor(ROSA_ATIVO);
        campo.setSelectionColor(VINHO_ESCURO);
        campo.setOpaque(true);
        campo.setBackground(SUPERFICIE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new BordaCampo(BORDA), BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        campo.setPreferredSize(TAMANHO_CAMPO);
        campo.setMinimumSize(new Dimension(110, TAMANHO_CAMPO.height));
    }

    static void campo(JPasswordField campo) {
        campo((JTextField) campo);
    }

    static void campoClaro(JTextField campo) {
        campo.setFont(FONTE_CAMPO);
        campo.setForeground(TEXTO_ESCURO);
        campo.setCaretColor(VINHO);
        campo.setSelectionColor(DESTAQUE_CLARO);
        campo.setOpaque(true);
        campo.setBackground(FUNDO_CAMPO_CLARO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new BordaCampo(BORDA_CLARA), BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        campo.setPreferredSize(new Dimension(240, 38));
        campo.setMinimumSize(new Dimension(100, 36));
    }

    static void campoClaro(JPasswordField campo) { campoClaro((JTextField) campo); }

    static void campoMonetario(JTextField campo) {
        campo.setHorizontalAlignment(SwingConstants.RIGHT);
        campo.setToolTipText("Valor em reais; use vírgula ou ponto decimal.");
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent evento) {
                String formatado = formatarValorMonetario(campo.getText());
                if (formatado != null) campo.setText(formatado);
            }
        });
    }

    static String formatarValorMonetario(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            BigDecimal valor = new BigDecimal(texto.trim().replace(',', '.'));
            return valor.setScale(Math.max(2, valor.scale())).toPlainString().replace('.', ',');
        } catch (NumberFormatException erro) {
            return null;
        }
    }

    static void componente(JComponent componente) {
        componente.setFont(FONTE_CORPO);
        componente.setForeground(TEXTO);
    }

    static void tabela(JTable tabela) {
        tabela(tabela, false);
    }

    static void tabelaClara(JTable tabela) { tabela(tabela, true); }

    private static void tabela(JTable tabela, boolean clara) {
        Color fundo = clara ? SUPERFICIE_CLARA : SUPERFICIE;
        Color texto = clara ? TEXTO_ESCURO : TEXTO;
        Color alternada = clara ? SUPERFICIE_CLARA_ALTERNADA : SUPERFICIE_ALTERNADA;
        Color cabecalho = clara ? new Color(226, 211, 216) : SUPERFICIE_CABECALHO;
        tabela.setFont(FONTE_CORPO);
        tabela.setForeground(texto);
        tabela.setBackground(fundo);
        tabela.setGridColor(clara ? BORDA_CLARA : LINHA_TABELA);
        tabela.setShowVerticalLines(false);
        tabela.setRowHeight(38);
        tabela.setSelectionBackground(clara ? new Color(222, 184, 193) : VINHO_ESCURO);
        tabela.setSelectionForeground(clara ? TEXTO_ESCURO : TEXTO);
        tabela.setFillsViewportHeight(true);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tabela, Object valor,
                    boolean selecionada, boolean foco, int linha, int coluna) {
                Component celula = super.getTableCellRendererComponent(
                        tabela, valor, selecionada, foco, linha, coluna);
                if (!selecionada) {
                    celula.setBackground(linha % 2 == 0 ? fundo : alternada);
                    celula.setForeground(texto);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return celula;
            }
        });
        tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabela.getTableHeader().setForeground(texto);
        tabela.getTableHeader().setBackground(cabecalho);
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tabela.getTableHeader().setReorderingAllowed(false);
    }

    static void cartao(JPanel painel) {
        painel.setBackground(SUPERFICIE_CARTAO);
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createCompoundBorder(
                new BordaArredondada(SUPERFICIE_CARTAO, BORDA, 22, 18),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    static void alinharValor(JLabel valor) {
        valor.setHorizontalAlignment(SwingConstants.RIGHT);
        valor.setFont(FONTE_VALOR);
        valor.setForeground(ROSA_AVERMELHADO);
    }

    static void cartaoCompacto(JPanel painel) {
        painel.setBackground(SUPERFICIE_CARTAO);
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createCompoundBorder(
                new BordaArredondada(SUPERFICIE_CARTAO, BORDA_SUAVE, 18, 12),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    }

    static void cartaoClaro(JPanel painel) { configurarCartao(painel, SUPERFICIE_CLARA, BORDA_CLARA, 22, 18); }

    static void cartaoCompactoClaro(JPanel painel) { configurarCartao(painel, SUPERFICIE_CLARA, BORDA_CLARA, 18, 12); }

    static void destacarCartaoOperacao(JPanel painel, boolean ativo) {
        Color borda = ativo ? VINHO : BORDA_CLARA;
        painel.setBorder(BorderFactory.createCompoundBorder(
                new BordaArredondada(SUPERFICIE_CLARA, borda, 18, 12),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    }

    private static void configurarCartao(JPanel painel, Color fundo, Color borda, int raio, int margem) {
        painel.setBackground(fundo);
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createCompoundBorder(
                new BordaArredondada(fundo, borda, raio, margem),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    }

    static JLabel rotuloCampo(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        rotulo.setForeground(TEXTO);
        return rotulo;
    }

    static JLabel rotuloCampoClaro(String texto) {
        JLabel rotulo = rotuloCampo(texto);
        rotulo.setForeground(TEXTO_ESCURO);
        return rotulo;
    }

    static JPanel cabecalhoTela(String titulo, String descricao) {
        JPanel textos = new JPanel(new java.awt.GridLayout(0, 1, 0, 5));
        textos.setOpaque(false);
        JLabel nome = new JLabel(titulo);
        nome.setFont(FONTE_TITULO);
        nome.setForeground(TEXTO);
        JLabel explicacao = new JLabel("<html><body style='width:440px'>" + descricao + "</body></html>");
        explicacao.setFont(FONTE_AUXILIAR);
        explicacao.setForeground(SECUNDARIO);
        textos.add(nome);
        textos.add(explicacao);
        return textos;
    }

    static JButton botao(String texto, boolean principal) {
        JButton botao = new BotaoArredondado(texto);
        botao.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        botao.setFocusPainted(false);
        botao.setOpaque(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setRolloverEnabled(true);
        botao.setBackground(principal ? VINHO : SUPERFICIE);
        botao.setForeground(principal ? Color.WHITE : TEXTO);
        botao.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        botao.setMinimumSize(new Dimension(0, TAMANHO_BOTAO.height));
        return botao;
    }

    static JButton botaoCompacto(String texto, boolean principal) {
        JButton botao = botao(texto, principal);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        botao.setBorder(BorderFactory.createEmptyBorder(6, 11, 6, 11));
        botao.setMinimumSize(TAMANHO_BOTAO_COMPACTO);
        return botao;
    }

    static JButton botaoComIcone(String texto, String rota, boolean principal) {
        JButton botao = botaoCompacto(texto, principal);
        botao.setIcon(new IconeNavegacao(rota, principal ? TEXTO : ROSA_ATIVO));
        botao.setHorizontalAlignment(AbstractButton.LEFT);
        botao.setIconTextGap(9);
        return botao;
    }

    static JButton cartaoAcao(String texto, String rota) {
        JButton botao = new BotaoCartaoAcao(texto);
        botao.setUI(new BasicButtonUI());
        botao.setFont(FONTE_AUXILIAR.deriveFont(Font.BOLD, 11f));
        botao.setForeground(TEXTO_ESCURO);
        botao.setIcon(new IconeNavegacao(rota, VINHO));
        botao.setHorizontalTextPosition(SwingConstants.CENTER);
        botao.setVerticalTextPosition(SwingConstants.BOTTOM);
        botao.setIconTextGap(8);
        botao.setFocusPainted(false);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setOpaque(false);
        botao.setRolloverEnabled(true);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(164, 90));
        botao.setMinimumSize(new Dimension(144, 82));
        botao.setToolTipText(texto);
        botao.getAccessibleContext().setAccessibleName(texto);
        return botao;
    }

    static JButton botaoFinanceiro(String texto, Color cor) {
        JButton botao = botao(texto, true);
        botao.setBackground(cor);
        return botao;
    }

    static void botaoNavegacao(AbstractButton botao, boolean selecionado) {
        botao.setFont(FONTE_MICRO);
        botao.setForeground(selecionado ? ROSA_ATIVO : SECUNDARIO);
        botao.setBackground(selecionado ? VINHO_ESCURO : SUPERFICIE);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(selecionado);
        botao.setContentAreaFilled(selecionado);
        botao.setVerticalTextPosition(AbstractButton.BOTTOM);
        botao.setHorizontalTextPosition(AbstractButton.CENTER);
        botao.setIcon(new IconeNavegacao(botao.getActionCommand(),
                selecionado ? ROSA_ATIVO : SECUNDARIO));
        botao.setIconTextGap(4);
        botao.setBorder(BorderFactory.createEmptyBorder(5, 1, 4, 1));
    }

    static void cartaoMensagem(JPanel painel, boolean usuario) {
        Color fundo = usuario ? FUNDO_CHAT_USUARIO : FUNDO_CHAT_ASSISTENTE;
        Color borda = usuario ? BORDA_CHAT_USUARIO : BORDA_SUAVE;
        painel.setBackground(fundo);
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createCompoundBorder(
                new BordaArredondada(fundo, borda, 16, 8),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    }

    static void botaoNavegacaoLateral(AbstractButton botao, boolean selecionado) {
        botao.setFont(FONTE_CORPO.deriveFont(Font.BOLD, 13f));
        botao.setForeground(selecionado ? ROSA_ATIVO : SECUNDARIO);
        botao.setBackground(selecionado ? VINHO_ESCURO : SUPERFICIE);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(selecionado);
        botao.setContentAreaFilled(selecionado);
        botao.setHorizontalAlignment(AbstractButton.LEFT);
        botao.setVerticalTextPosition(AbstractButton.CENTER);
        botao.setHorizontalTextPosition(AbstractButton.RIGHT);
        botao.setIcon(new IconeNavegacao(botao.getActionCommand(),
                selecionado ? ROSA_ATIVO : SECUNDARIO));
        botao.setIconTextGap(14);
        botao.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        botao.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    static void contas(JComboBox<Conta> combo) {
        configurarContas(combo, false);
    }

    static void contasClaras(JComboBox<Conta> combo) { configurarContas(combo, true); }

    private static void configurarContas(JComboBox<Conta> combo, boolean clara) {
        componente(combo);
        combo.setForeground(clara ? TEXTO_ESCURO : TEXTO);
        combo.setBackground(clara ? FUNDO_CAMPO_CLARO : SUPERFICIE);
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createCompoundBorder(
                new BordaCampo(clara ? BORDA_CLARA : BORDA), BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        combo.setPreferredSize(new Dimension(340, TAMANHO_CAMPO.height));
        combo.setMinimumSize(new Dimension(140, TAMANHO_CAMPO.height));
        ListCellRenderer<? super Conta> base = combo.getRenderer();
        combo.setRenderer((lista, conta, indice, selecionado, foco) -> {
            Component componente = base.getListCellRendererComponent(lista, conta, indice, selecionado, foco);
            if (conta != null && componente instanceof javax.swing.JLabel rotulo) {
                String identificacao = conta.getNumeroPublico() > 0
                        ? "Conta principal (" + conta.getNumeroPublicoFormatado() + ")" : "Conta principal";
                rotulo.setText(identificacao + " — " + conta.getTitular().getNome());
                rotulo.setOpaque(true);
                rotulo.setBackground(selecionado ? (clara ? new Color(222, 184, 193) : VINHO_ESCURO)
                        : (clara ? FUNDO_CAMPO_CLARO : SUPERFICIE));
                rotulo.setForeground(clara ? TEXTO_ESCURO : TEXTO);
                rotulo.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            }
            return componente;
        });
    }

    static JScrollPane rolagemNatural(JScrollPane rolagem) {
        rolagem.setBackground(SUPERFICIE);
        rolagem.getViewport().setBackground(SUPERFICIE);
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
            g.setColor(SOMBRA_CARTAO);
            g.fillRoundRect(x + 2, y + 3, largura - 3, altura - 3, raio + 2, raio + 2);
            g.setColor(SOMBRA_CARTAO_SUAVE);
            g.fillRoundRect(x + 1, y + 2, largura - 2, altura - 2, raio + 1, raio + 1);
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
            Color fundo = !isEnabled() ? FUNDO_DESATIVADO
                    : getModel().isPressed() && getModel().isArmed() ? getBackground().darker()
                    : getModel().isRollover() && getBackground().equals(SUPERFICIE)
                    ? SUPERFICIE_HOVER
                    : getModel().isRollover() ? getBackground().brighter() : getBackground();
            if (isEnabled() && getBackground().equals(VINHO)
                    && !(getModel().isPressed() && getModel().isArmed())) {
                Color topo = getModel().isRollover() ? ROSA_AVERMELHADO : VINHO;
                Color base = getModel().isRollover() ? VINHO : new Color(166, 34, 52);
                g.setPaint(new java.awt.GradientPaint(0, 0, topo, 0, getHeight(), base));
            } else {
                g.setColor(fundo);
            }
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            if (isFocusOwner()) {
                g.setColor(ROSA_ATIVO);
                g.setStroke(new BasicStroke(1.4f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            }
            g.dispose();
            super.paintComponent(grafico);
        }
    }

    private static final class BotaoCartaoAcao extends JButton {
        private BotaoCartaoAcao(String texto) { super(texto); }

        @Override protected void paintComponent(Graphics grafico) {
            Graphics2D g = (Graphics2D) grafico.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arco = 16;
            g.setColor(SOMBRA_CARTAO_SUAVE);
            g.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 3, arco, arco);
            Color topo = getModel().isPressed() && getModel().isArmed() ? DESTAQUE_CLARO
                    : getModel().isRollover() ? SUPERFICIE_CLARA_HOVER : FUNDO_CAMPO_CLARO;
            Color base = getModel().isRollover() ? SUPERFICIE_CLARA : SUPERFICIE_CLARA_ALTERNADA;
            g.setPaint(new java.awt.GradientPaint(0, 0, topo, getWidth(), getHeight(), base));
            g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 3, arco, arco);
            g.setColor(new Color(249, 226, 231, 190));
            g.fillOval(getWidth() / 2 - 17, 8, 34, 34);
            g.setColor(getModel().isRollover() || isFocusOwner() ? VINHO : BORDA_CLARA);
            g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 3, arco, arco);
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
            g.setColor(c.hasFocus() ? VINHO : cor);
            g.drawRoundRect(x, y, largura - 1, altura - 1, 12, 12);
            g.dispose();
        }
    }
}
