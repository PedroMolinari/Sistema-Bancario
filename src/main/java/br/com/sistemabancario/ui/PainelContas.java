package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

final class PainelContas extends JPanel {
    private final ModeloContas modelo = new ModeloContas();
    private final JTable tabela = new JTable(modelo);
    private final JLabel detalhe = new JLabel("Selecione uma conta para ver os detalhes.");
    private final JButton[] acoes;
    private final BiConsumer<String, Conta> navegar;

    PainelContas(Runnable atualizar, BiConsumer<String, Conta> navegar) {
        super(new BorderLayout(0, 16));
        this.navegar = navegar;
        setOpaque(false);
        add(titulo(), BorderLayout.NORTH);
        tabela.setRowHeight(32);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getSelectionModel().addListSelectionListener(evento -> atualizarDetalhe());
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA));
        JPanel conteudo = new JPanel(new BorderLayout(0, 12));
        conteudo.setOpaque(false);
        conteudo.add(rolagem, BorderLayout.CENTER);

        detalhe.setForeground(EstiloSwing.SECUNDARIO);
        detalhe.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        conteudo.add(detalhe, BorderLayout.NORTH);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);
        JButton recarregar = EstiloSwing.botao("Atualizar", false);
        recarregar.addActionListener(evento -> atualizar.run());
        barra.add(recarregar);
        acoes = new JButton[]{EstiloSwing.botao("Depositar", true), EstiloSwing.botao("Sacar", false),
                EstiloSwing.botao("Transferir", false), EstiloSwing.botao("Ver extrato", false)};
        String[] destinos = {"deposito", "saque", "transferencia", "extrato"};
        for (int indice = 0; indice < acoes.length; indice++) {
            final String destino = destinos[indice];
            acoes[indice].setEnabled(false);
            acoes[indice].addActionListener(evento -> {
                Conta conta = contaSelecionada();
                if (conta != null) this.navegar.accept(destino, conta);
            });
            barra.add(acoes[indice]);
        }
        conteudo.add(barra, BorderLayout.SOUTH);
        add(conteudo, BorderLayout.CENTER);
    }

    void definirContas(List<Conta> contas) {
        modelo.definirContas(contas);
        tabela.clearSelection();
        atualizarDetalhe();
    }

    Conta contaSelecionada() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : modelo.contaNaLinha(tabela.convertRowIndexToModel(linha));
    }

    private JPanel titulo() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        JLabel titulo = new JLabel("Contas");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titulo.setForeground(EstiloSwing.TEXTO);
        JLabel descricao = new JLabel("Consulte os saldos e acesse as operações da conta selecionada.");
        descricao.setForeground(EstiloSwing.SECUNDARIO);
        JPanel textos = new JPanel(new java.awt.GridLayout(0, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(descricao);
        painel.add(textos, BorderLayout.WEST);
        return painel;
    }

    private void atualizarDetalhe() {
        Conta conta = contaSelecionada();
        for (JButton acao : acoes) acao.setEnabled(conta != null);
        if (conta == null) {
            detalhe.setText(modelo.getRowCount() == 0 ? "Nenhuma conta cadastrada." : "Selecione uma conta para ver os detalhes.");
        } else {
            detalhe.setText("Titular: " + conta.getTitular().getNome() + "   |   Conta: "
                    + conta.getIdentificador() + "   |   Saldo: " + EstiloSwing.moeda(conta.getSaldo()));
        }
    }

    private static final class ModeloContas extends AbstractTableModel {
        private final String[] colunas = {"Titular", "Identificador da conta", "Saldo"};
        private List<Conta> contas = List.of();

        void definirContas(List<Conta> contas) {
            this.contas = List.copyOf(contas);
            fireTableDataChanged();
        }

        Conta contaNaLinha(int linha) { return contas.get(linha); }
        @Override public int getRowCount() { return contas.size(); }
        @Override public int getColumnCount() { return colunas.length; }
        @Override public String getColumnName(int coluna) { return colunas[coluna]; }
        @Override public Object getValueAt(int linha, int coluna) {
            Conta conta = contas.get(linha);
            return switch (coluna) {
                case 0 -> conta.getTitular().getNome();
                case 1 -> conta.getIdentificador().toString();
                case 2 -> EstiloSwing.moeda(conta.getSaldo());
                default -> throw new IndexOutOfBoundsException(coluna);
            };
        }
    }
}
