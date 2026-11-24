package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.swing.JPanel;

/** Conjunto das três operações, organizado em uma grade responsiva de cartões. */
final class PainelOperacoes extends JPanel {
    private final JPanel grade = new JPanel(new GridBagLayout());
    private final PainelOperacao deposito;
    private final PainelOperacao saque;
    private final PainelTransferencia transferencia;
    private final Map<Component, java.awt.Dimension> tamanhosNaturais = new IdentityHashMap<>();
    private int colunasAtuais;
    private String operacaoSelecionada;

    PainelOperacoes(PainelOperacao deposito, PainelOperacao saque, PainelTransferencia transferencia) {
        super(new BorderLayout(0, 10));
        this.deposito = deposito;
        this.saque = saque;
        this.transferencia = transferencia;
        for (Component formulario : new Component[] {deposito, saque, transferencia}) {
            tamanhosNaturais.put(formulario, formulario.getPreferredSize());
        }
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Ações", "Depósito, saque e transferência entre contas."), BorderLayout.NORTH);

        grade.setOpaque(false);
        grade.add(deposito);
        grade.add(saque);
        grade.add(transferencia);
        configurarGrade(0);
        add(grade, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent evento) { atualizarLayoutResponsivo(); }
        });
    }

    void atualizarLayoutResponsivo() { configurarGrade(getWidth()); }

    void selecionar(String operacao) {
        operacaoSelecionada = switch (operacao) {
            case "deposito", "saque", "transferencia" -> operacao;
            default -> null;
        };
        deposito.destacar("deposito".equals(operacaoSelecionada));
        saque.destacar("saque".equals(operacaoSelecionada));
        transferencia.destacar("transferencia".equals(operacaoSelecionada));
        javax.swing.JComponent alvo = switch (operacaoSelecionada == null ? "" : operacaoSelecionada) {
            case "deposito" -> deposito;
            case "saque" -> saque;
            case "transferencia" -> transferencia;
            default -> null;
        };
        if (alvo != null) javax.swing.SwingUtilities.invokeLater(() -> {
            atualizarLayoutResponsivo();
            alvo.scrollRectToVisible(new java.awt.Rectangle(0, 0, alvo.getWidth(), alvo.getHeight()));
        });
    }

    String operacaoSelecionada() { return operacaoSelecionada; }

    @Override public void doLayout() {
        atualizarLayoutResponsivo();
        super.doLayout();
    }

    private void configurarGrade(int largura) {
        int novasColunas = largura >= EstiloSwing.LARGURA_OPERACOES_TRES_COLUNAS ? 3
                : largura >= EstiloSwing.LARGURA_OPERACOES_DUAS_COLUNAS ? 2 : 1;
        if (novasColunas == colunasAtuais && grade.getComponentCount() == 3) return;
        colunasAtuais = novasColunas;
        grade.removeAll();
        Component[] ordem = novasColunas == 3 ? new Component[] {deposito, saque, transferencia}
                : novasColunas == 2 ? new Component[] {deposito, transferencia, saque}
                : new Component[] {deposito, saque, transferencia};
        int[] alturasPorLinha = new int[(ordem.length + novasColunas - 1) / novasColunas];
        for (int indice = 0; indice < ordem.length; indice++) {
            java.awt.Dimension tamanhoNatural = tamanhosNaturais.get(ordem[indice]);
            int linha = indice / novasColunas;
            alturasPorLinha[linha] = Math.max(alturasPorLinha[linha], tamanhoNatural.height);
        }
        for (int indice = 0; indice < ordem.length; indice++) {
            int coluna = indice % novasColunas;
            int linha = indice / novasColunas;
            java.awt.Dimension tamanhoNatural = tamanhosNaturais.get(ordem[indice]);
            ordem[indice].setPreferredSize(new java.awt.Dimension(tamanhoNatural.width,
                    novasColunas == 1 ? tamanhoNatural.height : alturasPorLinha[linha]));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = coluna;
            c.gridy = linha;
            c.weightx = 1;
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTH;
            c.insets = new Insets(linha > 0 ? 8 : 0, coluna > 0 ? 6 : 0, 0, 0);
            grade.add(ordem[indice], c);
        }
        GridBagConstraints preenchimento = new GridBagConstraints();
        preenchimento.gridx = 0;
        preenchimento.gridy = alturasPorLinha.length;
        preenchimento.gridwidth = novasColunas;
        preenchimento.weightx = 1;
        preenchimento.weighty = 1;
        preenchimento.fill = GridBagConstraints.BOTH;
        grade.add(javax.swing.Box.createGlue(), preenchimento);
        grade.revalidate();
        grade.repaint();
    }
}
