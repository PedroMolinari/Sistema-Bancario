package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;

final class PainelExtrato extends JPanel {
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final JComboBox<Conta> contas = new JComboBox<>();
    private final ModeloTransacoes modelo = new ModeloTransacoes();
    private final Consumer<Conta> consultar;
    private final BiConsumer<Conta, Path> exportar;
    private final JLabel estado = new JLabel("Selecione uma conta e consulte o extrato.");

    PainelExtrato(Consumer<Conta> consultar, BiConsumer<Conta, Path> exportar) {
        super(new BorderLayout(0, 16));
        this.consultar = consultar;
        this.exportar = exportar;
        setOpaque(false);
        add(titulo(), BorderLayout.NORTH);
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(30);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabela.getTableHeader().setReorderingAllowed(false);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA));
        JPanel corpo = new JPanel(new BorderLayout(0, 10));
        corpo.setOpaque(false);
        corpo.add(barraAcoes(), BorderLayout.NORTH);
        corpo.add(rolagem, BorderLayout.CENTER);
        estado.setForeground(EstiloSwing.SECUNDARIO);
        corpo.add(estado, BorderLayout.SOUTH);
        add(corpo, BorderLayout.CENTER);
    }

    void definirContas(List<Conta> novasContas) {
        UUID anterior = contaSelecionada() == null ? null : contaSelecionada().getIdentificador();
        contas.removeAllItems();
        novasContas.forEach(contas::addItem);
        selecionar(anterior);
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

    void definirTransacoes(List<Transacao> transacoes) {
        modelo.definirTransacoes(transacoes);
        estado.setText(transacoes.isEmpty() ? "Nenhuma transação registrada para esta conta." : "Transações carregadas: " + transacoes.size());
    }

    private JPanel barraAcoes() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(true);
        barra.setBackground(java.awt.Color.WHITE);
        EstiloSwing.contas(contas);
        contas.setPreferredSize(new java.awt.Dimension(440, 38));
        barra.add(new JLabel("Conta"));
        barra.add(contas);
        var atualizar = EstiloSwing.botao("Consultar extrato", true);
        atualizar.addActionListener(evento -> {
            Conta conta = contaSelecionada();
            if (conta == null) {
                JOptionPane.showMessageDialog(this, "Selecione uma conta.", "Conta necessária", JOptionPane.WARNING_MESSAGE);
                return;
            }
            consultar.accept(conta);
        });
        barra.add(atualizar);
        var salvar = EstiloSwing.botao("Exportar TXT", false);
        salvar.addActionListener(evento -> escolherDestino());
        barra.add(salvar);
        return barra;
    }

    private void escolherDestino() {
        Conta conta = contaSelecionada();
        if (conta == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma conta para exportar o extrato.", "Conta necessária", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Exportar extrato em TXT");
        seletor.setFileFilter(new FileNameExtensionFilter("Arquivo de texto (*.txt)", "txt"));
        seletor.setSelectedFile(Path.of("extrato-" + conta.getIdentificador().toString().substring(0, 8) + ".txt").toFile());
        if (seletor.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path arquivo = seletor.getSelectedFile().toPath();
        if (!arquivo.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".txt")) {
            arquivo = arquivo.resolveSibling(arquivo.getFileName() + ".txt");
        }
        if (Files.exists(arquivo) && JOptionPane.showConfirmDialog(this,
                "O arquivo já existe. Deseja substituí-lo?", "Confirmar sobrescrita",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        exportar.accept(conta, arquivo);
    }

    private Conta contaSelecionada() { return (Conta) contas.getSelectedItem(); }

    private JPanel titulo() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        JLabel rotulo = new JLabel("Extrato bancário");
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        rotulo.setForeground(EstiloSwing.TEXTO);
        painel.add(rotulo, BorderLayout.WEST);
        return painel;
    }

    private static final class ModeloTransacoes extends AbstractTableModel {
        private final String[] colunas = {"Data e hora", "Tipo", "Valor", "Origem", "Destino"};
        private List<Transacao> transacoes = List.of();

        void definirTransacoes(List<Transacao> transacoes) {
            this.transacoes = List.copyOf(transacoes);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return transacoes.size(); }
        @Override public int getColumnCount() { return colunas.length; }
        @Override public String getColumnName(int coluna) { return colunas[coluna]; }
        @Override public Object getValueAt(int linha, int coluna) {
            Transacao transacao = transacoes.get(linha);
            return switch (coluna) {
                case 0 -> transacao.dataHora().format(DATA_HORA);
                case 1 -> transacao.tipo();
                case 2 -> EstiloSwing.moeda(transacao.valor());
                case 3 -> transacao.contaOrigem().toString();
                case 4 -> transacao.contaDestino() == null ? "—" : transacao.contaDestino().toString();
                default -> throw new IndexOutOfBoundsException(coluna);
            };
        }
    }
}
