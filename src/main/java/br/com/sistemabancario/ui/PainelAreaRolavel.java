package br.com.sistemabancario.ui;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.LayoutManager;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/** Mantém a largura do cartão ativa igual à viewport e permite rolagem no eixo vertical. */
final class PainelAreaRolavel extends JPanel implements Scrollable {
    private static final String TELA_CHAT = "chat";
    private final Map<String, Component> telas = new LinkedHashMap<>();
    private String telaAtual;
    PainelAreaRolavel(LayoutManager layout) { super(layout); }

    void registrar(String identificador, Component componente) {
        if (identificador == null || identificador.isBlank()) throw new IllegalArgumentException("Identificador de tela vazio.");
        Objects.requireNonNull(componente, "O painel da tela é obrigatório.");
        if (telas.putIfAbsent(identificador, componente) != null) {
            throw new IllegalArgumentException("Tela já registrada: " + identificador);
        }
        add(componente, identificador);
    }

    void mostrar(String tela) {
        if (!telas.containsKey(tela)) throw new IllegalArgumentException("Tela não registrada: " + tela);
        ((java.awt.CardLayout) getLayout()).show(this, tela);
        telaAtual = tela;
        revalidate();
        repaint();
    }

    boolean registrada(String tela) { return telas.containsKey(tela); }
    String telaAtual() { return telaAtual; }
    Component componente(String tela) { return telas.get(tela); }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
        return 24;
    }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? Math.max(visible.height - 24, 24) : visible.width;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return TELA_CHAT.equals(telaAtual); }
}
