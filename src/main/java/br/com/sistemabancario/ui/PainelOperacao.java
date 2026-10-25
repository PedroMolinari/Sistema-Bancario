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
import java.util.function.BiConsumer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class PainelOperacao extends JPanel {
    enum Tipo { DEPOSITO, SAQUE }

    private final Tipo tipo;
    private final JComboBox<Conta> contas = new JComboBox<>();
    private final JTextField valor = new JTextField(24);

    PainelOperacao(Tipo tipo, BiConsumer<Conta, BigDecimal> executar) {
        super(new BorderLayout(0, 20));
        this.tipo = tipo;
        setOpaque(false);
        JLabel titulo = new JLabel(tipo == Tipo.DEPOSITO ? "Depósito" : "Saque");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titulo.setForeground(EstiloSwing.TEXTO);
        add(titulo, BorderLayout.NORTH);
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(java.awt.Color.WHITE);
        formulario.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 26, 24, 26));
        EstiloSwing.contas(contas);
        EstiloSwing.campo(valor);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 7, 12);
        formulario.add(new JLabel("Conta"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(contas, c);
        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(14, 0, 7, 12);
        formulario.add(new JLabel("Valor (R$)"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(valor, c);
        JLabel dica = new JLabel("Use ponto ou vírgula como separador decimal.");
        dica.setForeground(EstiloSwing.SECUNDARIO);
        c.gridx = 1; c.gridy = 2; c.insets = new Insets(2, 0, 0, 0);
        formulario.add(dica, c);
        var botao = EstiloSwing.botao(tipo == Tipo.DEPOSITO ? "Confirmar depósito" : "Confirmar saque", true);
        botao.addActionListener(evento -> {
            Conta conta = (Conta) contas.getSelectedItem();
            if (conta == null) {
                JOptionPane.showMessageDialog(this, "Cadastre ou selecione uma conta antes de continuar.",
                        "Conta necessária", JOptionPane.WARNING_MESSAGE);
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
            String acao = tipo == Tipo.DEPOSITO ? "depositar" : "sacar";
            int escolha = JOptionPane.showConfirmDialog(this,
                    "Confirma " + acao + " " + EstiloSwing.moeda(montante) + " na conta de "
                            + conta.getTitular().getNome() + "?",
                    "Confirmar operação", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (escolha == JOptionPane.YES_OPTION) executar.accept(conta, montante);
        });
        c.gridx = 1; c.gridy = 3; c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(20, 0, 0, 0);
        formulario.add(botao, c);
        add(formulario, BorderLayout.CENTER);
    }

    void definirContas(List<Conta> novasContas) {
        UUID selecionada = contaSelecionada() == null ? null : contaSelecionada().getIdentificador();
        contas.removeAllItems();
        novasContas.forEach(contas::addItem);
        selecionar(selecionada);
    }

    void selecionar(UUID contaId) {
        if (contaId == null) return;
        for (int i = 0; i < contas.getItemCount(); i++) {
            if (contas.getItemAt(i).getIdentificador().equals(contaId)) {
                contas.setSelectedIndex(i);
                return;
            }
        }
    }

    void operacaoConcluida() { valor.setText(""); }

    private Conta contaSelecionada() { return (Conta) contas.getSelectedItem(); }
}
