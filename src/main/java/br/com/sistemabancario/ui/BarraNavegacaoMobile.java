package br.com.sistemabancario.ui;

import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Barra inferior que mapeia as telas funcionais às cinco áreas principais do app. */
final class BarraNavegacaoMobile extends JPanel {
    private final Map<String, JButton> botoes = new LinkedHashMap<>();
    private final Consumer<String> navegar;

    BarraNavegacaoMobile(Consumer<String> navegar) {
        super(new java.awt.GridLayout(1, 5, 2, 0));
        this.navegar = navegar;
        setBackground(EstiloSwing.SUPERFICIE);
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloSwing.BORDA),
                javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        adicionar("Início", "inicio");
        adicionar("Contas", "contas");
        adicionar("Ações", "operacoes");
        adicionar("Extrato", "extrato");
        adicionar("Chat", "chat");
        setPreferredSize(new Dimension(0, 64));
    }

    void selecionar(String destino) {
        String aba = switch (destino) {
            case "cadastro" -> "contas";
            case "deposito", "saque", "transferencia" -> "operacoes";
            default -> destino;
        };
        botoes.forEach((rota, botao) -> EstiloSwing.botaoNavegacao(botao, rota.equals(aba)));
    }

    void configurarPerfil(boolean administrador) {
        JButton operacoes = botoes.get("operacoes");
        if (operacoes != null) {
            operacoes.setVisible(!administrador);
            ((java.awt.GridLayout) getLayout()).setColumns(administrador ? 4 : 5);
            revalidate();
        }
    }

    JButton botao(String rota) { return botoes.get(rota); }

    private void adicionar(String rotulo, String rota) {
        JButton botao = new JButton(rotulo);
        botao.setActionCommand(rota);
        EstiloSwing.botaoNavegacao(botao, false);
        botao.setToolTipText("Abrir tela " + rotulo);
        botao.getAccessibleContext().setAccessibleName("Abrir tela " + rotulo);
        botao.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(72, 56));
        botao.addActionListener(evento -> navegar.accept(rota));
        botoes.put(rota, botao);
        add(botao);
    }
}
