package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class PainelCadastroConta extends JPanel {
    private final JTextField nome = new JTextField(28);
    private final JTextField saldo = new JTextField("0.00", 28);

    PainelCadastroConta(BiConsumer<String, BigDecimal> cadastrar) {
        super(new BorderLayout(0, 20));
        setOpaque(false);
        add(titulo(), BorderLayout.NORTH);
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(true);
        formulario.setBackground(java.awt.Color.WHITE);
        formulario.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 26, 24, 26));
        EstiloSwing.campo(nome);
        EstiloSwing.campo(saldo);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 7, 10);
        formulario.add(new JLabel("Nome do titular"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(nome, c);
        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(12, 0, 7, 10);
        formulario.add(new JLabel("Saldo inicial (R$)"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(saldo, c);
        JLabel dica = new JLabel("Informe o valor com ponto ou vírgula decimal, por exemplo 250,00.");
        dica.setForeground(EstiloSwing.SECUNDARIO);
        c.gridx = 1; c.gridy = 2; c.insets = new Insets(2, 0, 0, 0);
        formulario.add(dica, c);
        JButton confirmar = EstiloSwing.botao("Cadastrar conta", true);
        confirmar.addActionListener(evento -> {
            String titular = nome.getText().trim();
            if (titular.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe o nome do titular.", "Validação", JOptionPane.WARNING_MESSAGE);
                nome.requestFocusInWindow();
                return;
            }
            BigDecimal valor;
            try {
                valor = lerValor(saldo.getText());
            } catch (NumberFormatException erro) {
                JOptionPane.showMessageDialog(this, "Informe um saldo inicial numérico válido.", "Validação", JOptionPane.WARNING_MESSAGE);
                saldo.requestFocusInWindow();
                return;
            }
            int escolha = JOptionPane.showConfirmDialog(this,
                    "Cadastrar conta para " + titular + " com saldo inicial de " + EstiloSwing.moeda(valor) + "?",
                    "Confirmar cadastro", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (escolha == JOptionPane.YES_OPTION) cadastrar.accept(titular, valor);
        });
        c.gridx = 1; c.gridy = 3; c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(20, 0, 0, 0);
        formulario.add(confirmar, c);
        add(formulario, BorderLayout.CENTER);
    }

    void cadastroConcluido() {
        nome.setText("");
        saldo.setText("0.00");
    }

    static BigDecimal lerValor(String texto) {
        String normalizado = texto == null ? "" : texto.trim().replace(',', '.');
        if (normalizado.isEmpty()) throw new NumberFormatException("Valor vazio");
        return new BigDecimal(normalizado);
    }

    private JPanel titulo() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        JLabel rotulo = new JLabel("Cadastro de conta");
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        rotulo.setForeground(EstiloSwing.TEXTO);
        painel.add(rotulo, BorderLayout.WEST);
        return painel;
    }
}
