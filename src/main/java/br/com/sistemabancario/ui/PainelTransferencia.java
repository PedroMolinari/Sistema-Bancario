package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class PainelTransferencia extends JPanel {
    @FunctionalInterface
    interface AcaoTransferencia { void executar(Conta origem, Conta destino, BigDecimal valor); }

    private final JComboBox<Conta> origem = new JComboBox<>();
    private final JComboBox<Conta> destino = new JComboBox<>();
    private final JTextField valor = new JTextField(24);

    PainelTransferencia(AcaoTransferencia transferir) {
        super(new BorderLayout(0, 20));
        setOpaque(false);
        JLabel titulo = new JLabel("Transferência");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titulo.setForeground(EstiloSwing.TEXTO);
        add(titulo, BorderLayout.NORTH);
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(java.awt.Color.WHITE);
        formulario.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 26, 24, 26));
        EstiloSwing.contas(origem);
        EstiloSwing.contas(destino);
        EstiloSwing.campo(valor);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 7, 12);
        formulario.add(new JLabel("Conta de origem"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(origem, c);
        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(14, 0, 7, 12);
        formulario.add(new JLabel("Conta de destino"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(destino, c);
        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(14, 0, 7, 12);
        formulario.add(new JLabel("Valor (R$)"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(valor, c);
        var botao = EstiloSwing.botao("Revisar e transferir", true);
        botao.addActionListener(evento -> {
            Conta contaOrigem = (Conta) origem.getSelectedItem();
            Conta contaDestino = (Conta) destino.getSelectedItem();
            if (contaOrigem == null || contaDestino == null) {
                JOptionPane.showMessageDialog(this, "São necessárias duas contas cadastradas.",
                        "Contas necessárias", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (contaOrigem.getIdentificador().equals(contaDestino.getIdentificador())) {
                JOptionPane.showMessageDialog(this, "A origem e o destino devem ser contas diferentes.",
                        "Transferência inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BigDecimal montante;
            try {
                montante = PainelCadastroConta.lerValor(valor.getText());
                if (montante.signum() <= 0) throw new NumberFormatException("O valor deve ser positivo");
            } catch (NumberFormatException erro) {
                JOptionPane.showMessageDialog(this, "Informe um valor maior que zero.", "Validação", JOptionPane.WARNING_MESSAGE);
                valor.requestFocusInWindow();
                return;
            }
            String revisao = "Revise a transferência:\n\nOrigem: " + contaOrigem.getTitular().getNome()
                    + " (" + contaOrigem.getIdentificador() + ")\nDestino: "
                    + contaDestino.getTitular().getNome() + " (" + contaDestino.getIdentificador()
                    + ")\nValor: " + EstiloSwing.moeda(montante) + "\n\nConfirmar operação?";
            if (JOptionPane.showConfirmDialog(this, revisao, "Revisão da transferência",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                transferir.executar(contaOrigem, contaDestino, montante);
            }
        });
        c.gridx = 1; c.gridy = 3; c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(20, 0, 0, 0);
        formulario.add(botao, c);
        add(formulario, BorderLayout.CENTER);
    }

    void definirContas(List<Conta> contas) {
        UUID origemAnterior = idSelecionado(origem);
        UUID destinoAnterior = idSelecionado(destino);
        origem.removeAllItems();
        destino.removeAllItems();
        contas.forEach(conta -> { origem.addItem(conta); destino.addItem(conta); });
        selecionar(origem, origemAnterior);
        selecionar(destino, destinoAnterior);
        if (contas.size() > 1 && (destino.getSelectedItem() == origem.getSelectedItem())) destino.setSelectedIndex(1);
    }

    void selecionarOrigem(UUID contaId) { selecionar(origem, contaId); }
    void operacaoConcluida() { valor.setText(""); }

    private static UUID idSelecionado(JComboBox<Conta> combo) {
        Conta conta = (Conta) combo.getSelectedItem();
        return conta == null ? null : conta.getIdentificador();
    }

    private static void selecionar(JComboBox<Conta> combo, UUID id) {
        if (id == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getIdentificador().equals(id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
}
