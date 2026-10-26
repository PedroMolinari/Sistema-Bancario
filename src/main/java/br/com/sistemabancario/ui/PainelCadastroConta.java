package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class PainelCadastroConta extends JPanel {
    private final JTextField nome = new JTextField(28);
    private final JTextField saldo = new JTextField("0.00", 28);

    PainelCadastroConta(BiConsumer<String, BigDecimal> cadastrar) {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Cadastro de conta",
                "Informe os dados do titular e defina o saldo para iniciar o relacionamento bancário."), BorderLayout.NORTH);
        JPanel formulario = new JPanel(new GridBagLayout());
        EstiloSwing.cartao(formulario);
        nome.setColumns(36);
        saldo.setColumns(36);
        EstiloSwing.campo(nome);
        EstiloSwing.campo(saldo);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(2, 0, 18, 0);
        formulario.add(tituloBloco(), c);
        c.gridwidth = 1; c.gridy = 1; c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE; c.weightx = 0; c.insets = new Insets(0, 0, 8, 16);
        formulario.add(EstiloSwing.rotuloCampo("Nome do titular"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(nome, c);
        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(14, 0, 8, 16);
        formulario.add(EstiloSwing.rotuloCampo("Saldo inicial (R$)"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(saldo, c);
        JLabel dica = new JLabel("Informe o valor com ponto ou vírgula decimal, por exemplo 250,00.");
        dica.setForeground(EstiloSwing.SECUNDARIO);
        dica.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloSwing.AZUL_CLARO),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        dica.setBackground(new java.awt.Color(246, 250, 255));
        dica.setOpaque(true);
        c.gridx = 1; c.gridy = 3; c.insets = new Insets(8, 0, 0, 0);
        formulario.add(dica, c);
        JButton confirmar = EstiloSwing.botao("Cadastrar conta", true);
        confirmar.addActionListener(evento -> {
            String titular = nome.getText().trim();
            if (titular.isEmpty()) {
                DialogoConfirmacao.aviso(this, "Dados incompletos", "Informe o nome do titular.");
                nome.requestFocusInWindow();
                return;
            }
            BigDecimal valor;
            try {
                valor = lerValor(saldo.getText());
            } catch (NumberFormatException erro) {
                DialogoConfirmacao.aviso(this, "Saldo inválido", "Informe um saldo inicial numérico válido.");
                saldo.requestFocusInWindow();
                return;
            }
            boolean confirmarCadastro = DialogoConfirmacao.confirmar(this, "Confirmar cadastro",
                    "Cadastrar conta para " + titular + " com saldo inicial de " + EstiloSwing.moeda(valor) + "?",
                    "Confirmar cadastro", false);
            if (confirmarCadastro) cadastrar.accept(titular, valor);
        });
        JPanel barra = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        barra.setOpaque(false);
        barra.add(confirmar);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(22, 0, 0, 0);
        formulario.add(barra, c);
        c.gridy = 5; c.weighty = 1; c.fill = GridBagConstraints.VERTICAL;
        JPanel expansor = new JPanel();
        expansor.setOpaque(false);
        formulario.add(expansor, c);
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

    private JPanel tituloBloco() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        JLabel rotulo = new JLabel("Dados da conta");
        rotulo.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 17));
        rotulo.setForeground(EstiloSwing.TEXTO);
        painel.add(rotulo, BorderLayout.WEST);
        return painel;
    }
}
