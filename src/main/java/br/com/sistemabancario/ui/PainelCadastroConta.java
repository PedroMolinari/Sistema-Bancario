package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Cadastro conjunto de cliente e primeira conta, preservando o fluxo de negócio existente. */
final class PainelCadastroConta extends JPanel {
    @FunctionalInterface
    interface ConfirmacaoCadastro { boolean confirmar(String titular, BigDecimal saldoInicial); }

    private final JTextField nome = new JTextField();
    private final JTextField saldo = new JTextField("0.00");
    private final ConfirmacaoCadastro confirmacao;

    PainelCadastroConta(BiConsumer<String, BigDecimal> cadastrar) {
        this(cadastrar, null);
    }

    PainelCadastroConta(BiConsumer<String, BigDecimal> cadastrar, ConfirmacaoCadastro confirmacao) {
        super(new BorderLayout(0, 16));
        this.confirmacao = confirmacao != null ? confirmacao : this::confirmarComDialogo;
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Novo cliente e conta",
                "Informe o titular e o saldo inicial da conta."), BorderLayout.NORTH);

        JPanel cartao = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        EstiloSwing.cartao(cartao);
        JLabel titulo = new JLabel("Dados do cadastro");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        titulo.setForeground(EstiloSwing.TEXTO);
        cartao.add(titulo);
        cartao.add(EstiloSwing.rotuloCampo("Nome do titular"));
        EstiloSwing.campo(nome);
        nome.setPreferredSize(new Dimension(280, 48));
        cartao.add(nome);
        cartao.add(EstiloSwing.rotuloCampo("Saldo inicial (R$)"));
        EstiloSwing.campo(saldo);
        saldo.setPreferredSize(new Dimension(280, 48));
        cartao.add(saldo);
        JLabel dica = new JLabel("Use ponto ou vírgula como separador decimal.");
        dica.setForeground(EstiloSwing.SECUNDARIO);
        dica.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        cartao.add(dica);

        JButton confirmar = EstiloSwing.botao("Cadastrar cliente e conta", true);
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
            boolean confirmou = this.confirmacao.confirmar(titular, valor);
            if (confirmou) cadastrar.accept(titular, valor);
        });
        JPanel acao = new JPanel(new BorderLayout());
        acao.setOpaque(false);
        acao.add(confirmar, BorderLayout.CENTER);
        cartao.add(acao);
        add(cartao, BorderLayout.CENTER);
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

    private boolean confirmarComDialogo(String titular, BigDecimal valor) {
        return DialogoConfirmacao.confirmar(this, "Confirmar cadastro",
                "Cadastrar cliente " + titular + " com saldo inicial de " + EstiloSwing.moeda(valor) + "?",
                "Confirmar cadastro", false);
    }
}
