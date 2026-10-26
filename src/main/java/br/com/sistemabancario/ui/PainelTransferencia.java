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
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class PainelTransferencia extends JPanel {
    @FunctionalInterface
    interface AcaoTransferencia { void executar(Conta origem, Conta destino, BigDecimal valor); }

    private final JComboBox<Conta> origem = new JComboBox<>();
    private final JComboBox<Conta> destino = new JComboBox<>();
    private final JTextField valor = new JTextField(24);

    PainelTransferencia(AcaoTransferencia transferir) {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Transferência",
                "Escolha as contas e revise os dados antes de confirmar a movimentação."), BorderLayout.NORTH);
        JPanel formulario = new JPanel(new GridBagLayout());
        EstiloSwing.cartao(formulario);
        EstiloSwing.contas(origem);
        EstiloSwing.contas(destino);
        EstiloSwing.campo(valor);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        valor.setPreferredSize(new java.awt.Dimension(300, 60));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(2, 0, 20, 0);
        JLabel tituloBloco = new JLabel("Dados da transferência");
        tituloBloco.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        tituloBloco.setForeground(EstiloSwing.TEXTO);
        formulario.add(tituloBloco, c);
        c.gridwidth = 1; c.gridy = 1; c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE; c.weightx = 0; c.insets = new Insets(0, 0, 8, 18);
        formulario.add(EstiloSwing.rotuloCampo("Conta de origem"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(origem, c);
        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(15, 0, 8, 18);
        formulario.add(EstiloSwing.rotuloCampo("Conta de destino"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(destino, c);
        c.gridx = 0; c.gridy = 3; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(15, 0, 8, 18);
        formulario.add(EstiloSwing.rotuloCampo("Valor (R$)"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(valor, c);
        JLabel dica = new JLabel("Após revisar origem, destino e valor, confirme a transferência.");
        dica.setForeground(EstiloSwing.SECUNDARIO);
        dica.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloSwing.AZUL_CLARO),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        dica.setBackground(new java.awt.Color(246, 250, 255));
        dica.setOpaque(true);
        c.gridx = 1; c.gridy = 4; c.insets = new Insets(9, 0, 0, 0);
        formulario.add(dica, c);
        var botao = EstiloSwing.botao("Revisar e transferir", true);
        botao.addActionListener(evento -> {
            Conta contaOrigem = (Conta) origem.getSelectedItem();
            Conta contaDestino = (Conta) destino.getSelectedItem();
            if (contaOrigem == null || contaDestino == null) {
                DialogoConfirmacao.aviso(this, "Contas necessárias", "São necessárias duas contas cadastradas.");
                return;
            }
            if (contaOrigem.getIdentificador().equals(contaDestino.getIdentificador())) {
                DialogoConfirmacao.aviso(this, "Transferência inválida",
                        "A origem e o destino devem ser contas diferentes.");
                return;
            }
            BigDecimal montante;
            try {
                montante = PainelCadastroConta.lerValor(valor.getText());
                if (montante.signum() <= 0) throw new NumberFormatException("O valor deve ser positivo");
            } catch (NumberFormatException erro) {
                DialogoConfirmacao.aviso(this, "Valor inválido", "Informe um valor maior que zero.");
                valor.requestFocusInWindow();
                return;
            }
            String revisao = "Revise a transferência:\n\nOrigem: " + contaOrigem.getTitular().getNome()
                    + " (" + contaOrigem.getIdentificadorCurto() + ")\nDestino: "
                    + contaDestino.getTitular().getNome() + " (" + contaDestino.getIdentificadorCurto()
                    + ")\nValor: " + EstiloSwing.moeda(montante) + "\n\nConfirmar operação?";
            if (DialogoConfirmacao.confirmar(this, "Revisar transferência", revisao,
                    "Confirmar transferência", false)) {
                transferir.executar(contaOrigem, contaDestino, montante);
            }
        });
        JPanel barra = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        barra.setOpaque(false);
        barra.add(botao);
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(22, 0, 0, 0);
        formulario.add(barra, c);
        c.gridy = 6; c.weighty = 1; c.fill = GridBagConstraints.VERTICAL;
        JPanel expansor = new JPanel();
        expansor.setOpaque(false);
        formulario.add(expansor, c);
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
