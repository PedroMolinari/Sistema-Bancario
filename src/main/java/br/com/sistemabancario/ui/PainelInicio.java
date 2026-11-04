package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/** Resumo inicial composto apenas por contas e movimentações já consultadas pelo controlador. */
final class PainelInicio extends JPanel {
    private final Consumer<String> navegar;
    private final JLabel totalContas = new JLabel("0");
    private final JLabel saldoTotal = new JLabel("R$ 0,00");
    private final JLabel totalMovimentacoes = new JLabel("0");
    private final JLabel estadoMovimentacoes = new JLabel("Movimentações recentes aparecerão aqui quando houver dados.");
    private final ModeloRecentes modelo = new ModeloRecentes();
    private final JTable tabela = new JTable(modelo);
    private final JPanel conteudoRecentes = new JPanel(new java.awt.CardLayout());

    PainelInicio(Consumer<String> navegar) {
        super(new BorderLayout(0, 18));
        this.navegar = navegar;
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Visão geral", "Resumo das contas e acesso rápido às funções do sistema."),
                BorderLayout.NORTH);

        JPanel corpo = new JPanel(new GridBagLayout());
        corpo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 16, 0);
        corpo.add(cartoesResumo(), c);
        c.gridy = 1; c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 16, 0);
        corpo.add(cartaoAtalhos(), c);
        c.gridy = 2; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        corpo.add(cartaoRecentes(), c);
        JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(corpo,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        add(rolagem, BorderLayout.CENTER);
    }

    void definirContas(List<Conta> contas) {
        totalContas.setText(Integer.toString(contas.size()));
        BigDecimal total = contas.stream().map(Conta::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        saldoTotal.setText(EstiloSwing.moeda(total));
    }

    void carregandoMovimentacoes() {
        estadoMovimentacoes.setText("Consultando movimentações recentes…");
        estadoMovimentacoes.setForeground(EstiloSwing.SECUNDARIO);
        ((java.awt.CardLayout) conteudoRecentes.getLayout()).show(conteudoRecentes, "vazio");
    }

    void definirTransacoes(List<Transacao> transacoes) {
        modelo.definirTransacoes(transacoes);
        totalMovimentacoes.setText(Integer.toString(transacoes.size()));
        boolean temDados = !transacoes.isEmpty();
        ((java.awt.CardLayout) conteudoRecentes.getLayout()).show(conteudoRecentes,
                temDados ? "dados" : "vazio");
        if (!temDados) {
            estadoMovimentacoes.setText("Movimentações recentes aparecerão aqui quando houver dados.");
            estadoMovimentacoes.setForeground(EstiloSwing.SECUNDARIO);
        }
    }

    void falhaMovimentacoes() {
        ((java.awt.CardLayout) conteudoRecentes.getLayout()).show(conteudoRecentes, "vazio");
        estadoMovimentacoes.setText("Não foi possível carregar as movimentações recentes.");
        estadoMovimentacoes.setForeground(EstiloSwing.AVISO);
    }

    private JPanel cartoesResumo() {
        JPanel faixa = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        faixa.setOpaque(false);
        faixa.add(cartaoResumo("CONTAS", totalContas, "Contas cadastradas"));
        faixa.add(cartaoResumo("SALDO TOTAL", saldoTotal, "Soma dos saldos atuais"));
        faixa.add(cartaoResumo("MOVIMENTAÇÕES", totalMovimentacoes, "Transações recentes"));
        return faixa;
    }

    private JPanel cartaoResumo(String titulo, JLabel valor, String descricao) {
        JPanel cartao = new JPanel(new BorderLayout(0, 9));
        EstiloSwing.cartao(cartao);
        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        rotulo.setForeground(EstiloSwing.SECUNDARIO);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        valor.setForeground(EstiloSwing.TEXTO);
        JLabel apoio = new JLabel(descricao);
        apoio.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        apoio.setForeground(EstiloSwing.SECUNDARIO);
        cartao.add(rotulo, BorderLayout.NORTH);
        cartao.add(valor, BorderLayout.CENTER);
        cartao.add(apoio, BorderLayout.SOUTH);
        return cartao;
    }

    private JPanel cartaoAtalhos() {
        JPanel cartao = new JPanel(new BorderLayout(0, 12));
        EstiloSwing.cartao(cartao);
        JLabel titulo = new JLabel("Acesso rápido");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titulo.setForeground(EstiloSwing.TEXTO);
        cartao.add(titulo, BorderLayout.NORTH);

        JPanel botoes = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        botoes.setOpaque(false);
        adicionarAtalho(botoes, "Minhas contas", "contas", false);
        adicionarAtalho(botoes, "Novo cliente e conta", "cadastro", false);
        adicionarAtalho(botoes, "Fazer depósito", "deposito", true);
        adicionarAtalho(botoes, "Fazer saque", "saque", false);
        adicionarAtalho(botoes, "Transferir", "transferencia", false);
        adicionarAtalho(botoes, "Consultar extrato", "extrato", false);
        cartao.add(botoes, BorderLayout.CENTER);
        return cartao;
    }

    private void adicionarAtalho(JPanel destino, String texto, String tela, boolean principal) {
        JButton botao = EstiloSwing.botao(texto, principal);
        botao.setHorizontalAlignment(JButton.LEFT);
        botao.addActionListener(evento -> navegar.accept(tela));
        destino.add(botao);
    }

    private JPanel cartaoRecentes() {
        JPanel cartao = new JPanel(new BorderLayout(0, 12));
        EstiloSwing.cartao(cartao);
        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setOpaque(false);
        JLabel rotulo = new JLabel("Movimentações recentes");
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        rotulo.setForeground(EstiloSwing.TEXTO);
        JButton verExtrato = EstiloSwing.botao("Abrir extrato", false);
        verExtrato.addActionListener(evento -> navegar.accept("extrato"));
        titulo.add(rotulo, BorderLayout.WEST);
        titulo.add(verExtrato, BorderLayout.EAST);
        cartao.add(titulo, BorderLayout.NORTH);

        EstiloSwing.tabela(tabela);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabela.setFillsViewportHeight(true);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(115);
        tabela.getColumnModel().getColumn(2).setCellRenderer(new RendererValor());
        JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(tabela));
        rolagem.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA));
        conteudoRecentes.add(rolagem, "dados");
        estadoMovimentacoes.setFont(EstiloSwing.FONTE_CORPO);
        estadoMovimentacoes.setForeground(EstiloSwing.SECUNDARIO);
        estadoMovimentacoes.setHorizontalAlignment(JLabel.CENTER);
        estadoMovimentacoes.setBorder(BorderFactory.createEmptyBorder(22, 12, 22, 12));
        JPanel vazio = new JPanel(new BorderLayout());
        vazio.setBackground(EstiloSwing.FUNDO_INFO);
        vazio.add(estadoMovimentacoes, BorderLayout.CENTER);
        conteudoRecentes.add(vazio, "vazio");
        ((java.awt.CardLayout) conteudoRecentes.getLayout()).show(conteudoRecentes, "vazio");
        cartao.add(conteudoRecentes, BorderLayout.CENTER);
        return cartao;
    }

    private static final class ModeloRecentes extends AbstractTableModel {
        private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        private final String[] colunas = {"Data", "Movimentação", "Valor"};
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
                case 1 -> switch (transacao.tipo()) {
                    case DEPOSITO -> "Entrada · Depósito";
                    case SAQUE -> "Saída · Saque";
                    case TRANSFERENCIA -> "Transferência";
                };
                case 2 -> EstiloSwing.moeda(transacao.valor());
                default -> throw new IndexOutOfBoundsException(coluna);
            };
        }
    }

    private static final class RendererValor extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selecionado,
                                                        boolean foco, int linha, int coluna) {
            Component componente = super.getTableCellRendererComponent(
                    table, value, selecionado, foco, linha, coluna);
            String tipo = String.valueOf(table.getModel().getValueAt(table.convertRowIndexToModel(linha), 1));
            if (!selecionado) {
                setForeground(tipo.startsWith("Entrada") ? EstiloSwing.SUCESSO
                        : tipo.startsWith("Saída") ? EstiloSwing.ERRO : EstiloSwing.TEXTO);
            }
            setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            return componente;
        }
    }
}
