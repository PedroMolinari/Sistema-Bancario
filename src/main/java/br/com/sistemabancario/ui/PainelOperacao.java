package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class PainelOperacao extends JPanel {
    enum Tipo { DEPOSITO, SAQUE }

    private final Tipo tipo;
    private final JComboBox<Conta> contas = new JComboBox<>();
    private final JTextField valor = new JTextField();

    PainelOperacao(Tipo tipo, BiConsumer<Conta, BigDecimal> executar) {
        super(new BorderLayout(0, 16));
        this.tipo = tipo;
        setOpaque(false);
        boolean deposito = tipo == Tipo.DEPOSITO;
        add(EstiloSwing.cabecalhoTela(deposito ? "Depósito" : "Saque",
                deposito ? "Adicione recursos à conta selecionada."
                        : "Informe a conta e o valor que deseja retirar."), BorderLayout.NORTH);
        JPanel cartao = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        EstiloSwing.cartao(cartao);
        JLabel titulo = new JLabel("Dados da operação");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        titulo.setForeground(EstiloSwing.TEXTO);
        cartao.add(titulo);
        cartao.add(EstiloSwing.rotuloCampo("Conta"));
        EstiloSwing.contas(contas);
        contas.setPreferredSize(new Dimension(280, 48));
        cartao.add(contas);
        cartao.add(EstiloSwing.rotuloCampo("Valor (R$)"));
        EstiloSwing.campo(valor);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        valor.setForeground(deposito ? EstiloSwing.SUCESSO : EstiloSwing.ERRO);
        valor.setPreferredSize(new Dimension(280, 60));
        cartao.add(valor);
        JLabel dica = new JLabel("Use ponto ou vírgula como separador decimal.");
        dica.setForeground(EstiloSwing.SECUNDARIO);
        cartao.add(dica);
        var botao = EstiloSwing.botaoFinanceiro(
                deposito ? "Confirmar depósito" : "Confirmar saque",
                deposito ? EstiloSwing.SUCESSO : EstiloSwing.ERRO);
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
            String operacao = deposito ? "depósito" : "saque";
            boolean confirmou = DialogoConfirmacao.confirmar(this, "Confirmar " + operacao,
                    "Confirma " + operacao + " de " + EstiloSwing.moeda(montante) + " na conta de "
                            + conta.getTitular().getNome() + " (" + conta.getIdentificadorCurto() + ")?",
                    deposito ? "Confirmar depósito" : "Confirmar saque", false);
            if (confirmou) executar.accept(conta, montante);
        });
        cartao.add(botao);
        add(cartao, BorderLayout.CENTER);
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
