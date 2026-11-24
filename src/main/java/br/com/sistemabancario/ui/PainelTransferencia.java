package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Formulário de transferência com identificação pública, sem expor UUIDs internos. */
final class PainelTransferencia extends JPanel {
    @FunctionalInterface
    interface AcaoTransferencia { void executar(Conta origem, long destinoNumero, BigDecimal valor); }

    private final JComboBox<Conta> origem = new JComboBox<>();
    private final JTextField destino = new JTextField();
    private final JTextField valor = new JTextField();
    private final JPanel cartao = new JPanel();

    PainelTransferencia(AcaoTransferencia transferir) { this(transferir, false); }

    PainelTransferencia(AcaoTransferencia transferir, boolean compacto) {
        super(new BorderLayout(0, compacto ? 0 : 12));
        setOpaque(false);
        if (!compacto) {
            add(EstiloSwing.cabecalhoTela("Transferência",
                    "Informe o número público da conta de destino e revise os dados."), BorderLayout.NORTH);
        }
        cartao.setLayout(new javax.swing.BoxLayout(cartao, javax.swing.BoxLayout.Y_AXIS));
        if (compacto) EstiloSwing.cartaoCompactoClaro(cartao);
        else EstiloSwing.cartaoClaro(cartao);
        JLabel titulo = new JLabel("Transferência",
                new IconeNavegacao("transferencia", EstiloSwing.VINHO), JLabel.LEADING);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, compacto ? 15 : 17));
        titulo.setForeground(EstiloSwing.TEXTO_ESCURO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(titulo);
        cartao.add(javax.swing.Box.createVerticalStrut(compacto ? 6 : 12));

        adicionarCampo(cartao, "Conta de origem", origem);
        EstiloSwing.contasClaras(origem);
        origem.setPreferredSize(new Dimension(280, compacto ? 36 : 42));
        origem.setMinimumSize(new Dimension(100, compacto ? 36 : 42));
        origem.setMaximumSize(new Dimension(Integer.MAX_VALUE, compacto ? 36 : 42));
        cartao.add(javax.swing.Box.createVerticalStrut(compacto ? 4 : 10));

        adicionarCampo(cartao, "Número da conta de destino", destino);
        EstiloSwing.campoClaro(destino);
        destino.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        destino.setToolTipText("Informe o número público da conta, exibido na página Contas.");
        destino.getAccessibleContext().setAccessibleName("Número da conta de destino");
        destino.putClientProperty("JTextField.placeholderText", "Ex.: 000123");
        destino.setPreferredSize(new Dimension(280, compacto ? 36 : 42));
        destino.setMaximumSize(new Dimension(Integer.MAX_VALUE, compacto ? 36 : 42));
        cartao.add(javax.swing.Box.createVerticalStrut(compacto ? 4 : 10));

        adicionarCampo(cartao, "Valor (R$)", valor);
        EstiloSwing.campoClaro(valor);
        EstiloSwing.campoMonetario(valor);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, compacto ? 18 : 21));
        valor.setPreferredSize(new Dimension(280, compacto ? 38 : 46));
        valor.setMaximumSize(new Dimension(Integer.MAX_VALUE, compacto ? 38 : 46));

        cartao.add(javax.swing.Box.createVerticalStrut(compacto ? 6 : 12));
        cartao.add(criarBotao(this, transferir, origem, destino, valor, compacto));
        add(cartao, BorderLayout.CENTER);
        setPreferredSize(new Dimension(280, cartao.getPreferredSize().height));
    }

    private static void adicionarCampo(JPanel cartao, String rotulo, java.awt.Component campo) {
        JLabel label = EstiloSwing.rotuloCampoClaro(rotulo);
        label.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(label);
        ((javax.swing.JComponent) campo).setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(campo);
    }

    void definirContas(List<Conta> contas) {
        UUID anterior = idSelecionado(origem);
        origem.removeAllItems();
        contas.forEach(origem::addItem);
        selecionar(origem, anterior);
    }

    void selecionarOrigem(UUID contaId) { selecionar(origem, contaId); }
    void operacaoConcluida() { valor.setText(""); destino.setText(""); }
    void destacar(boolean ativo) { EstiloSwing.destacarCartaoOperacao(cartao, ativo); }

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

    private static javax.swing.JButton criarBotao(PainelTransferencia painel, AcaoTransferencia transferir,
                                                   JComboBox<Conta> origem, JTextField destino,
                                                   JTextField valor, boolean compacto) {
        javax.swing.JButton botao = EstiloSwing.botaoComIcone("Revisar e transferir", "transferencia", true);
        botao.setPreferredSize(new Dimension(220, compacto ? 36 : 42));
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, compacto ? 36 : 42));
        botao.setAlignmentX(LEFT_ALIGNMENT);
        botao.addActionListener(evento -> {
                Conta contaOrigem = (Conta) origem.getSelectedItem();
                if (contaOrigem == null) {
                    DialogoConfirmacao.aviso(painel, "Conta de origem necessária",
                            "Selecione uma conta de origem.");
                    return;
                }
                long contaDestino;
                try {
                    contaDestino = Long.parseLong(destino.getText().trim());
                    if (contaDestino <= 0) throw new NumberFormatException();
                } catch (NumberFormatException erro) {
                    DialogoConfirmacao.aviso(painel, "Destino inválido",
                            "Informe um número público válido para a conta de destino.");
                    destino.requestFocusInWindow();
                    return;
                }
                if (contaOrigem.getNumeroPublico() == contaDestino) {
                    DialogoConfirmacao.aviso(painel, "Transferência inválida",
                            "A origem e o destino devem ser contas diferentes.");
                    return;
                }
                BigDecimal montante;
                try {
                    montante = PainelCadastroConta.lerValor(valor.getText());
                    if (montante.signum() <= 0) throw new NumberFormatException("O valor deve ser positivo");
                } catch (NumberFormatException erro) {
                    DialogoConfirmacao.aviso(painel, "Valor inválido", "Informe um valor maior que zero.");
                    valor.requestFocusInWindow();
                    return;
                }
                String revisao = "Revise a transferência:\n\nOrigem: " + contaOrigem.getTitular().getNome()
                        + " (" + contaOrigem.getNomePublico() + ")\nDestino: Conta "
                        + Conta.formatarNumeroPublico(contaDestino) + "\nValor: "
                        + EstiloSwing.moeda(montante) + "\n\nConfirmar operação?";
                if (DialogoConfirmacao.confirmar(painel, "Revisar transferência", revisao,
                        "Confirmar transferência", false)) {
                    transferir.executar(contaOrigem, contaDestino, montante);
                }
        });
        return botao;
    }
}
