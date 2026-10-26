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
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class PainelOperacao extends JPanel {
    enum Tipo { DEPOSITO, SAQUE }

    private final Tipo tipo;
    private final JComboBox<Conta> contas = new JComboBox<>();
    private final JTextField valor = new JTextField(24);

    PainelOperacao(Tipo tipo, BiConsumer<Conta, BigDecimal> executar) {
        super(new BorderLayout(0, 22));
        this.tipo = tipo;
        setOpaque(false);
        boolean deposito = tipo == Tipo.DEPOSITO;
        add(EstiloSwing.cabecalhoTela(deposito ? "Depósito" : "Saque",
                deposito ? "Adicione recursos à conta selecionada com segurança."
                        : "Informe a conta e o valor que deseja retirar."), BorderLayout.NORTH);
        JPanel formulario = new JPanel(new GridBagLayout());
        EstiloSwing.cartao(formulario);
        EstiloSwing.contas(contas);
        EstiloSwing.campo(valor);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        valor.setPreferredSize(new java.awt.Dimension(300, 62));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(2, 0, 20, 0);
        JLabel tituloBloco = new JLabel("Dados da operação");
        tituloBloco.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        tituloBloco.setForeground(EstiloSwing.TEXTO);
        formulario.add(tituloBloco, c);
        c.gridwidth = 1; c.gridy = 1; c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE; c.weightx = 0; c.insets = new Insets(0, 0, 8, 18);
        formulario.add(EstiloSwing.rotuloCampo("Conta"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(contas, c);
        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.insets = new Insets(20, 0, 8, 18);
        formulario.add(EstiloSwing.rotuloCampo("Valor (R$)"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(valor, c);
        JLabel dica = new JLabel("Digite o valor em reais. Use ponto ou vírgula como separador decimal.");
        dica.setForeground(EstiloSwing.SECUNDARIO);
        dica.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloSwing.AZUL_CLARO),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        dica.setBackground(new java.awt.Color(246, 250, 255));
        dica.setOpaque(true);
        c.gridx = 1; c.gridy = 3; c.insets = new Insets(9, 0, 0, 0);
        formulario.add(dica, c);
        var botao = EstiloSwing.botao(tipo == Tipo.DEPOSITO ? "Confirmar depósito" : "Confirmar saque", true);
        botao.addActionListener(evento -> {
            Conta conta = (Conta) contas.getSelectedItem();
            if (conta == null) {
                DialogoConfirmacao.aviso(this, "Conta necessária",
                        "Cadastre ou selecione uma conta antes de continuar.");
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
            String operacao = tipo == Tipo.DEPOSITO ? "depósito" : "saque";
            boolean confirmou = DialogoConfirmacao.confirmar(this, "Confirmar " + operacao,
                    "Confirma " + operacao + " de " + EstiloSwing.moeda(montante) + " na conta de "
                            + conta.getTitular().getNome() + " (" + conta.getIdentificadorCurto() + ")?",
                    tipo == Tipo.DEPOSITO ? "Confirmar depósito" : "Confirmar saque", false);
            if (confirmou) executar.accept(conta, montante);
        });
        JPanel barra = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        barra.setOpaque(false);
        barra.add(botao);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(22, 0, 0, 0);
        formulario.add(barra, c);
        c.gridy = 5; c.weighty = 1; c.fill = GridBagConstraints.VERTICAL;
        JPanel expansor = new JPanel();
        expansor.setOpaque(false);
        formulario.add(expansor, c);
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
