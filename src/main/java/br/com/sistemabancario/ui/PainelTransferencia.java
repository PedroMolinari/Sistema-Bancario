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

final class PainelTransferencia extends JPanel {
    @FunctionalInterface
    interface AcaoTransferencia { void executar(Conta origem, Conta destino, BigDecimal valor); }

    private final JComboBox<Conta> origem = new JComboBox<>();
    private final JComboBox<Conta> destino = new JComboBox<>();
    private final JTextField valor = new JTextField();

    PainelTransferencia(AcaoTransferencia transferir) {
        super(new BorderLayout(0, 16));
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Transferência",
                "Escolha as contas e revise os dados antes de confirmar."), BorderLayout.NORTH);
        JPanel cartao = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        EstiloSwing.cartao(cartao);
        JLabel titulo = new JLabel("Dados da transferência");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        titulo.setForeground(EstiloSwing.TEXTO);
        cartao.add(titulo);
        cartao.add(EstiloSwing.rotuloCampo("Conta de origem"));
        EstiloSwing.contas(origem);
        origem.setPreferredSize(new Dimension(280, 48));
        cartao.add(origem);
        cartao.add(EstiloSwing.rotuloCampo("Conta de destino"));
        EstiloSwing.contas(destino);
        destino.setPreferredSize(new Dimension(280, 48));
        cartao.add(destino);
        cartao.add(EstiloSwing.rotuloCampo("Valor (R$)"));
        EstiloSwing.campo(valor);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        valor.setPreferredSize(new Dimension(280, 60));
        cartao.add(valor);
        JLabel dica = new JLabel("Revise origem, destino e valor antes de confirmar.");
        dica.setForeground(EstiloSwing.SECUNDARIO);
        cartao.add(dica);
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
        cartao.add(botao);
        add(cartao, BorderLayout.CENTER);
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
