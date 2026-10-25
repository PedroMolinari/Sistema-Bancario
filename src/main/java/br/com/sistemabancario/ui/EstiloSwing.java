package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

final class EstiloSwing {
    static final Color MARINHO = new Color(24, 42, 74);
    static final Color AZUL = new Color(42, 105, 180);
    static final Color FUNDO = new Color(244, 247, 251);
    static final Color TEXTO = new Color(35, 45, 60);
    static final Color SECUNDARIO = new Color(103, 115, 132);
    static final Color BORDA = new Color(222, 228, 236);

    private EstiloSwing() { }

    static String moeda(java.math.BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    static void campo(JTextField campo) {
        campo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        campo.setForeground(TEXTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    static void componente(JComponent componente) {
        componente.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        componente.setForeground(TEXTO);
    }

    static JButton botao(String texto, boolean principal) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        botao.setFocusPainted(false);
        botao.setOpaque(true);
        botao.setBackground(principal ? AZUL : Color.WHITE);
        botao.setForeground(principal ? Color.WHITE : TEXTO);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(principal ? AZUL : BORDA),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        return botao;
    }

    static void contas(JComboBox<Conta> combo) {
        componente(combo);
        ListCellRenderer<? super Conta> base = combo.getRenderer();
        combo.setRenderer((lista, conta, indice, selecionado, foco) -> {
            Component componente = base.getListCellRendererComponent(lista, conta, indice, selecionado, foco);
            if (conta != null && componente instanceof javax.swing.JLabel rotulo) {
                rotulo.setText(conta.getTitular().getNome() + " — " + conta.getIdentificador()
                        + " — " + moeda(conta.getSaldo()));
            }
            return componente;
        });
    }
}
