package br.com.sistemabancario.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Alterna entre a tela de entrada geral e o conteúdo principal do aplicativo. */
final class PainelRaizMobile extends JPanel {
    private static final String ACESSO = "acesso";
    private static final String APLICACAO = "aplicacao";
    private final CardLayout layout = new CardLayout();
    private final JButton entrar;
    private boolean aplicativoVisivel;

    PainelRaizMobile(Component aplicativo, Runnable aoEntrar) {
        super();
        Objects.requireNonNull(aplicativo);
        Objects.requireNonNull(aoEntrar);
        setLayout(layout);
        setBackground(EstiloSwing.FUNDO);
        PainelAcesso acesso = new PainelAcesso(() -> {
            mostrarAplicativo();
            aoEntrar.run();
        });
        entrar = acesso.botaoEntrar();
        add(acesso, ACESSO);
        add(aplicativo, APLICACAO);
        mostrarAcesso();
    }

    void mostrarAcesso() {
        aplicativoVisivel = false;
        layout.show(this, ACESSO);
    }

    boolean aplicativoVisivel() { return aplicativoVisivel; }
    JButton botaoEntrar() { return entrar; }

    private void mostrarAplicativo() {
        aplicativoVisivel = true;
        layout.show(this, APLICACAO);
    }
}
