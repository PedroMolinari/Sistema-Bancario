package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

final class PainelExtrato extends JPanel {
    private static final String DADOS = "dados";
    private static final String VAZIO = "vazio";
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final JComboBox<Conta> contas = new JComboBox<>();
    private final ModeloTransacoes modelo = new ModeloTransacoes();
    private final Consumer<Conta> consultar;
    private final BiConsumer<Conta, Path> exportar;
    private final JLabel estado = new JLabel("Selecione uma conta e consulte o extrato.");
    private final JPanel cartoesTabela = new JPanel(new CardLayout());

    PainelExtrato(Consumer<Conta> consultar, BiConsumer<Conta, Path> exportar) {
        super(new BorderLayout(0, 22));
        this.consultar = consultar;
        this.exportar = exportar;
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Extrato bancário",
                "Consulte as movimentações registradas e exporte o histórico em arquivo TXT."), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new GridBagLayout());
        corpo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 16, 0);
        corpo.add(cartaoSelecao(), c);
        c.gridy = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 12, 0);
        corpo.add(cartaoTabela(), c);
        c.gridy = 2; c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        estado.setFont(EstiloSwing.FONTE_CORPO);
        estado.setForeground(EstiloSwing.SECUNDARIO);
        corpo.add(estado, c);
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
        Conta conta = contaSelecionada();
        modelo.definirTransacoes(conta == null ? null : conta.getIdentificador(), transacoes);
        CardLayout cards = (CardLayout) cartoesTabela.getLayout();
        cards.show(cartoesTabela, transacoes.isEmpty() ? VAZIO : DADOS);
        estado.setText(transacoes.isEmpty() ? "Nenhuma transação registrada para esta conta."
                : "Movimentações carregadas: " + transacoes.size());
    }

    private JPanel cartaoSelecao() {
        JPanel cartao = new JPanel(new BorderLayout(0, 10));
        EstiloSwing.cartao(cartao);
        EstiloSwing.contas(contas);
        JPanel campos = new JPanel(new BorderLayout(0, 7));
        campos.setOpaque(false);
        campos.add(EstiloSwing.rotuloCampo("Conta"), BorderLayout.NORTH);
        campos.add(contas, BorderLayout.CENTER);
        cartao.add(campos, BorderLayout.NORTH);
        JPanel botoes = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        botoes.setOpaque(false);
        var atualizar = EstiloSwing.botao("Consultar", true);
        atualizar.addActionListener(evento -> {
            Conta conta = contaSelecionada();
            if (conta == null) {
                DialogoConfirmacao.aviso(this, "Conta necessária", "Selecione uma conta.");
                return;
            }
            consultar.accept(conta);
        });
        var salvar = EstiloSwing.botao("Exportar TXT", false);
        salvar.addActionListener(evento -> escolherDestino());
        botoes.add(atualizar);
        botoes.add(salvar);
        cartao.add(botoes, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel cartaoTabela() {
        JPanel cartao = new JPanel(new BorderLayout(0, 14));
        EstiloSwing.cartao(cartao);
        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setOpaque(false);
        JLabel rotulo = new JLabel("Movimentações da conta");
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        rotulo.setForeground(EstiloSwing.TEXTO);
        titulo.add(rotulo, BorderLayout.WEST);
        cartao.add(titulo, BorderLayout.NORTH);

        JTable tabela = new JTable(modelo);
        EstiloSwing.tabela(tabela);
        DefaultTableCellRenderer rendererMovimentacao = new RendererMovimentacao();
        tabela.getColumnModel().getColumn(1).setCellRenderer(rendererMovimentacao);
        tabela.getColumnModel().getColumn(2).setCellRenderer(new RendererValor());
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(125);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(130);
        JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(tabela));
        rolagem.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA));
        cartoesTabela.add(rolagem, DADOS);
        JLabel vazio = new JLabel("Nenhuma movimentação para exibir. Consulte o extrato de uma conta.", JLabel.CENTER);
        vazio.setFont(EstiloSwing.FONTE_CORPO);
        vazio.setForeground(EstiloSwing.SECUNDARIO);
        vazio.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(38, 22, 38, 22)));
        cartoesTabela.add(vazio, VAZIO);
        ((CardLayout) cartoesTabela.getLayout()).show(cartoesTabela, VAZIO);
        cartao.add(cartoesTabela, BorderLayout.CENTER);
        return cartao;
    }

    private void escolherDestino() {
        Conta conta = contaSelecionada();
        if (conta == null) {
            DialogoConfirmacao.aviso(this, "Conta necessária", "Selecione uma conta para exportar o extrato.");
            return;
        }
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Exportar extrato em TXT");
        seletor.setFileFilter(new FileNameExtensionFilter("Arquivo de texto (*.txt)", "txt"));
        seletor.setSelectedFile(Path.of("extrato-" + conta.getIdentificadorCurto() + ".txt").toFile());
        if (seletor.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path arquivo = seletor.getSelectedFile().toPath();
        if (!arquivo.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".txt")) {
            arquivo = arquivo.resolveSibling(arquivo.getFileName() + ".txt");
        }
        if (Files.exists(arquivo) && !DialogoConfirmacao.confirmar(this, "Substituir arquivo",
                "O arquivo já existe. Deseja substituí-lo?", "Substituir", true)) return;
        exportar.accept(conta, arquivo);
    }

    private Conta contaSelecionada() { return (Conta) contas.getSelectedItem(); }

    private static final class ModeloTransacoes extends AbstractTableModel {
        private final String[] colunas = {"Data e hora", "Tipo", "Valor", "Origem", "Destino"};
        private List<Transacao> transacoes = List.of();
        private UUID contaId;

        void definirTransacoes(UUID contaId, List<Transacao> transacoes) {
            this.contaId = contaId;
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
                case 1 -> rotuloMovimentacao(transacao);
                case 2 -> entrada(transacao) ? "+ " + EstiloSwing.moeda(transacao.valor())
                        : "− " + EstiloSwing.moeda(transacao.valor());
                case 3 -> Conta.formatarIdentificador(transacao.contaOrigem());
                case 4 -> transacao.contaDestino() == null ? "—"
                        : Conta.formatarIdentificador(transacao.contaDestino());
                default -> throw new IndexOutOfBoundsException(coluna);
            };
        }

        private String rotuloMovimentacao(Transacao transacao) {
            return (entrada(transacao) ? "Entrada · " : "Saída · ") + switch (transacao.tipo()) {
                case DEPOSITO -> "Depósito";
                case SAQUE -> "Saque";
                case TRANSFERENCIA -> "Transferência";
            };
        }

        private boolean entrada(Transacao transacao) {
            return switch (transacao.tipo()) {
                case DEPOSITO -> true;
                case SAQUE -> false;
                case TRANSFERENCIA -> contaId != null && contaId.equals(transacao.contaDestino());
            };
        }
    }

    private static class RendererMovimentacao extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable tabela, Object valor,
                boolean selecionada, boolean foco, int linha, int coluna) {
            java.awt.Component componente = super.getTableCellRendererComponent(
                    tabela, valor, selecionada, foco, linha, coluna);
            if (!selecionada) {
                setForeground(String.valueOf(valor).startsWith("Entrada")
                        ? EstiloSwing.SUCESSO : EstiloSwing.ERRO);
            }
            return componente;
        }
    }

    private static final class RendererValor extends RendererMovimentacao {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable tabela, Object valor,
                boolean selecionada, boolean foco, int linha, int coluna) {
            java.awt.Component componente = super.getTableCellRendererComponent(
                    tabela, valor, selecionada, foco, linha, coluna);
            setHorizontalAlignment(SwingConstants.RIGHT);
            return componente;
        }
    }
}
