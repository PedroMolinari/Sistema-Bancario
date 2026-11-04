package br.com.sistemabancario.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/** Ícones vetoriais pequenos para os atalhos laterais, sem recursos externos. */
final class IconeNavegacao implements Icon {
    private final String tela;
    private final Color cor;

    IconeNavegacao(String tela, boolean selecionado) {
        this.tela = tela;
        cor = selecionado ? Color.WHITE : new Color(226, 205, 209);
    }

    IconeNavegacao(String tela, Color cor) {
        this.tela = tela;
        this.cor = cor;
    }

    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }

    @Override
    public void paintIcon(Component componente, Graphics grafico, int x, int y) {
        Graphics2D g = (Graphics2D) grafico.create();
        g.translate(x, y);
        g.setColor(cor);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (tela) {
            case "inicio" -> {
                g.drawLine(2, 9, 10, 2); g.drawLine(10, 2, 18, 9);
                g.drawRoundRect(4, 8, 12, 10, 2, 2); g.drawRect(8, 12, 4, 6);
            }
            case "contas" -> {
                g.drawOval(7, 2, 6, 6); g.drawArc(3, 9, 14, 10, 0, 180);
                g.drawOval(1, 5, 4, 4); g.drawOval(15, 5, 4, 4);
            }
            case "cadastro" -> {
                g.drawOval(2, 2, 16, 16); g.drawLine(10, 6, 10, 14); g.drawLine(6, 10, 14, 10);
            }
            case "deposito" -> {
                g.drawLine(10, 2, 10, 12); g.drawLine(6, 8, 10, 12); g.drawLine(14, 8, 10, 12);
                g.drawLine(3, 13, 3, 18); g.drawLine(3, 18, 17, 18); g.drawLine(17, 18, 17, 13);
            }
            case "saque" -> {
                g.drawLine(10, 12, 10, 2); g.drawLine(6, 6, 10, 2); g.drawLine(14, 6, 10, 2);
                g.drawLine(3, 13, 3, 18); g.drawLine(3, 18, 17, 18); g.drawLine(17, 18, 17, 13);
            }
            case "transferencia" -> {
                g.drawLine(2, 6, 17, 6); g.drawLine(13, 3, 17, 6); g.drawLine(13, 9, 17, 6);
                g.drawLine(18, 14, 3, 14); g.drawLine(7, 11, 3, 14); g.drawLine(7, 17, 3, 14);
            }
            case "extrato" -> {
                g.drawRoundRect(4, 2, 12, 16, 2, 2); g.drawLine(7, 7, 13, 7);
                g.drawLine(7, 10, 13, 10); g.drawLine(7, 13, 11, 13);
            }
            case "chat" -> {
                g.drawRoundRect(2, 3, 16, 12, 4, 4); g.drawLine(6, 15, 4, 18);
                g.fillOval(6, 8, 2, 2); g.fillOval(9, 8, 2, 2); g.fillOval(12, 8, 2, 2);
            }
            case "operacoes" -> {
                g.drawRoundRect(2, 3, 16, 14, 3, 3);
                g.drawLine(5, 8, 15, 8); g.drawLine(5, 12, 11, 12);
            }
            default -> g.drawOval(4, 4, 12, 12);
        }
        g.dispose();
    }
}
