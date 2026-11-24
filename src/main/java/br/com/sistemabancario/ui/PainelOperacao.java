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

    private final JComboBox<Conta> contas = new JComboBox<>();
    private final JTextField valor = new JTextField();
    private final JPanel cartao = new JPanel();

    PainelOperacao(Tipo tipo, BiConsumer<Conta, BigDecimal> executar) {
        this(tipo, executar, false);
    }

    PainelOperacao(Tipo tipo, BiConsumer<Conta, BigDecimal> executar, boolean compacto) {
        super(new BorderLayout(0, compacto ? 0 : 12));
        setOpaque(false);
        boolean deposito = tipo == Tipo.DEPOSITO;
        if (!compacto) {
            add(EstiloSwing.cabecalhoTela(deposito ? "Depósito" : "Saque",
                    deposito ? "Adicione recursos à conta selecionada."
                            : "Informe a conta e o valor que deseja retirar."), BorderLayout.NORTH);
        }
        cartao.setLayout(new javax.swing.BoxLayout(cartao, javax.swing.BoxLayout.Y_AXIS));
        if (compacto) EstiloSwing.cartaoCompactoClaro(cartao);
        else EstiloSwing.cartaoClaro(cartao);
        JLabel titulo = new JLabel(compacto ? (deposito ? "Depósito" : "Saque") : "Dados da operação",
                new IconeNavegacao(deposito ? "deposito" : "saque", EstiloSwing.VINHO), JLabel.LEADING);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, compacto ? 15 : 17));
        titulo.setForeground(EstiloSwing.TEXTO_ESCURO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(titulo);
        cartao.add(javax.swing.Box.createVerticalStrut(compacto ? 8 : 12));
        JLabel rotuloConta = EstiloSwing.rotuloCampoClaro("Conta");
        rotuloConta.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(rotuloConta);
        EstiloSwing.contasClaras(contas);
        contas.setPreferredSize(new Dimension(280, compacto ? 36 : 42));
        contas.setMinimumSize(new Dimension(100, compacto ? 36 : 42));
        contas.setMaximumSize(new Dimension(Integer.MAX_VALUE, compacto ? 36 : 42));
        contas.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(contas);
        cartao.add(javax.swing.Box.createVerticalStrut(compacto ? 7 : 12));
        JLabel rotuloValor = EstiloSwing.rotuloCampoClaro("Valor (R$)");
        rotuloValor.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(rotuloValor);
        EstiloSwing.campoClaro(valor);
        EstiloSwing.campoMonetario(valor);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, compacto ? 18 : 21));
        valor.setForeground(deposito ? EstiloSwing.ROSA_TEXTO : EstiloSwing.TEXTO_ESCURO);
        valor.setPreferredSize(new Dimension(280, compacto ? 38 : 48));
        valor.setMaximumSize(new Dimension(Integer.MAX_VALUE, compacto ? 38 : 48));
        valor.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(valor);
        if (!compacto) {
            JLabel dica = new JLabel("Use ponto ou vírgula como separador decimal.");
            dica.setForeground(EstiloSwing.SECUNDARIO);
            dica.setAlignmentX(LEFT_ALIGNMENT);
            cartao.add(javax.swing.Box.createVerticalStrut(6));
            cartao.add(dica);
        }
        var botao = EstiloSwing.botaoComIcone(
                deposito ? "Confirmar depósito" : "Confirmar saque", deposito ? "deposito" : "saque", true);
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
                            + conta.getTitular().getNome() + " (" + conta.getNomePublico() + ")?",
                    deposito ? "Confirmar depósito" : "Confirmar saque", false);
            if (confirmou) executar.accept(conta, montante);
        });
        botao.setPreferredSize(new Dimension(220, compacto ? 36 : 42));
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, compacto ? 36 : 42));
        botao.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(javax.swing.Box.createVerticalStrut(compacto ? 8 : 12));
        cartao.add(botao);
        if (compacto) {
            setPreferredSize(new Dimension(280, cartao.getPreferredSize().height));
        }
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

    void destacar(boolean ativo) { EstiloSwing.destacarCartaoOperacao(cartao, ativo); }

    private Conta contaSelecionada() { return (Conta) contas.getSelectedItem(); }
}
