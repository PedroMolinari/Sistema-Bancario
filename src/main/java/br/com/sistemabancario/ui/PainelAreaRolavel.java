package br.com.sistemabancario.ui;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/** Mantém a largura do cartão ativa igual à viewport e permite rolagem no eixo vertical. */
final class PainelAreaRolavel extends JPanel implements Scrollable {
    PainelAreaRolavel(LayoutManager layout) { super(layout); }

    void mostrar(String tela) { ((java.awt.CardLayout) getLayout()).show(this, tela); }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
        return 24;
    }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? Math.max(visible.height - 24, 24) : visible.width;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
