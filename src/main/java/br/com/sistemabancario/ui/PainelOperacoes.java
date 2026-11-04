package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Atalhos das operações disponíveis na navegação compacta. */
final class PainelOperacoes extends JPanel {
    PainelOperacoes(Consumer<String> navegar) {
        super(new BorderLayout(0, 16));
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Operações", "Escolha o que deseja fazer."), BorderLayout.NORTH);
        JPanel cartao = new JPanel(new GridLayout(0, 1, 0, 12));
        EstiloSwing.cartao(cartao);
        adicionar(cartao, "Depositar", "deposito", navegar);
        adicionar(cartao, "Sacar", "saque", navegar);
        adicionar(cartao, "Transferir", "transferencia", navegar);
        add(cartao, BorderLayout.CENTER);
    }

    private static void adicionar(JPanel cartao, String texto, String destino, Consumer<String> navegar) {
        JButton botao = EstiloSwing.botao(texto, destino.equals("deposito"));
        botao.setHorizontalAlignment(JButton.LEFT);
        botao.setPreferredSize(new java.awt.Dimension(280, 54));
        botao.addActionListener(evento -> navegar.accept(destino));
        cartao.add(botao);
    }
}
