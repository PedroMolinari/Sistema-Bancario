package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

final class EstiloSwing {
    static final Color MARINHO = new Color(24, 42, 74);
    static final Color AZUL = new Color(42, 105, 180);
    static final Color AZUL_ATIVO = new Color(40, 76, 126);
    static final Color AZUL_CLARO = new Color(229, 239, 251);
    static final Color FUNDO = new Color(244, 247, 251);
    static final Color TEXTO = new Color(35, 45, 60);
    static final Color SECUNDARIO = new Color(103, 115, 132);
    static final Color BORDA = new Color(222, 228, 236);
    static final Color LINHA_TABELA = new Color(235, 239, 245);
    static final Color SUCESSO = new Color(38, 121, 84);
    static final Color ERRO = new Color(174, 58, 64);
    static final Color AVISO = new Color(152, 97, 22);
    static final Font FONTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 27);
    static final Font FONTE_CORPO = new Font(Font.SANS_SERIF, Font.PLAIN, 15);

    private EstiloSwing() { }

    static String moeda(java.math.BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    static void campo(JTextField campo) {
        campo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        campo.setForeground(TEXTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA), BorderFactory.createEmptyBorder(10, 13, 10, 13)));
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
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA), BorderFactory.createEmptyBorder(20, 22, 20, 22)));
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
        JLabel explicacao = new JLabel(descricao);
        explicacao.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        explicacao.setForeground(SECUNDARIO);
        textos.add(nome);
        textos.add(explicacao);
        return textos;
    }

    static JButton botao(String texto, boolean principal) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        botao.setFocusPainted(false);
        botao.setOpaque(true);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setBackground(principal ? AZUL : Color.WHITE);
        botao.setForeground(principal ? Color.WHITE : TEXTO);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(principal ? AZUL : BORDA),
                BorderFactory.createEmptyBorder(11, 18, 11, 18)));
        return botao;
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
}
