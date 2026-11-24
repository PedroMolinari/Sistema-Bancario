package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Navegação lateral para janelas desktop, com os mesmos destinos da barra compacta. */
final class BarraNavegacaoLateral extends JPanel {
    private final Map<String, JButton> botoes = new LinkedHashMap<>();
    private final Consumer<String> navegar;

    BarraNavegacaoLateral(Consumer<String> navegar) {
        super(new BorderLayout(0, 24));
        this.navegar = navegar;
        setBackground(EstiloSwing.SUPERFICIE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(20, 12, 16, 12)));
        setPreferredSize(new Dimension(218, 0));

        JPanel marca = new JPanel(new BorderLayout(12, 0));
        marca.setOpaque(false);
        JLabel monograma = new JLabel("SB", JLabel.CENTER);
        monograma.setOpaque(true);
        monograma.setBackground(EstiloSwing.VINHO);
        monograma.setForeground(EstiloSwing.TEXTO);
        monograma.setFont(EstiloSwing.FONTE_SECAO);
        monograma.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloSwing.ROSA_ATIVO),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JPanel nome = new JPanel(new java.awt.GridLayout(0, 1, 0, 3));
        nome.setOpaque(false);
        JLabel produto = new JLabel("Sistema Bancário");
        produto.setFont(EstiloSwing.FONTE_SECAO);
        produto.setForeground(EstiloSwing.TEXTO);
        JLabel contexto = new JLabel("PAINEL FINANCEIRO");
        contexto.setFont(EstiloSwing.FONTE_MICRO);
        contexto.setForeground(EstiloSwing.SECUNDARIO);
        nome.add(produto);
        nome.add(contexto);
        marca.add(monograma, BorderLayout.WEST);
        marca.add(nome, BorderLayout.CENTER);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new javax.swing.BoxLayout(menu, javax.swing.BoxLayout.Y_AXIS));
        JLabel rotulo = new JLabel("MENU PRINCIPAL");
        rotulo.setFont(EstiloSwing.FONTE_MICRO);
        rotulo.setForeground(EstiloSwing.SECUNDARIO);
        rotulo.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 0));
        menu.add(rotulo);
        adicionar(menu, "Início", "inicio");
        adicionar(menu, "Contas", "contas");
        adicionar(menu, "Ações", "operacoes");
        adicionar(menu, "Extrato", "extrato");
        adicionar(menu, "Assistente", "chat");

        add(marca, BorderLayout.NORTH);
        add(menu, BorderLayout.CENTER);
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rodape.setOpaque(false);
        JLabel seguro = new JLabel("PAINEL FINANCEIRO LOCAL");
        seguro.setFont(EstiloSwing.FONTE_AUXILIAR);
        seguro.setForeground(EstiloSwing.SECUNDARIO);
        rodape.add(seguro);
        add(rodape, BorderLayout.SOUTH);
    }

    void selecionar(String destino) {
        String rota = switch (destino) {
            case "cadastro" -> "contas";
            case "deposito", "saque", "transferencia" -> "operacoes";
            default -> destino;
        };
        botoes.forEach((chave, botao) -> EstiloSwing.botaoNavegacaoLateral(botao, chave.equals(rota)));
    }

    void configurarPerfil(boolean administrador) {
        JButton operacoes = botoes.get("operacoes");
        if (operacoes != null) operacoes.setVisible(!administrador);
        revalidate();
    }

    JButton botao(String rota) { return botoes.get(rota); }

    private void adicionar(JPanel menu, String rotulo, String rota) {
        JButton botao = new JButton(rotulo);
        botao.setActionCommand(rota);
        EstiloSwing.botaoNavegacaoLateral(botao, false);
        botao.setToolTipText(rotulo);
        botao.getAccessibleContext().setAccessibleName("Abrir tela " + rotulo);
        botao.addActionListener(evento -> navegar.accept(rota));
        botoes.put(rota, botao);
        menu.add(botao);
        menu.add(javax.swing.Box.createVerticalStrut(4));
    }
}
