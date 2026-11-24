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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private final JPanel botoesAtalho = new JPanel(new GridBagLayout());
    private final Map<String, JButton> atalhos = new LinkedHashMap<>();
    private int colunasAtalho;
    private int quantidadeAtalhos;

    PainelInicio(Consumer<String> navegar) {
        super(new BorderLayout(0, 12));
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
        c.insets = new Insets(0, 0, 12, 0);
        corpo.add(cartaoAtalhos(), c);
        c.gridy = 2; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        corpo.add(cartaoRecentes(), c);
        add(corpo, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent evento) { ajustarGradeAtalhos(); }
        });
        ajustarGradeAtalhos();
    }

    void definirContas(List<Conta> contas) {
        totalContas.setText(Integer.toString(contas.size()));
        BigDecimal total = contas.stream().map(Conta::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        saldoTotal.setText(EstiloSwing.moeda(total));
    }

    void configurarPerfil(boolean administrador) {
        JButton contas = atalhos.get("contas");
        if (contas != null) {
            contas.setText(administrador ? "Todas as contas" : "Minhas contas");
            contas.setToolTipText(contas.getText());
            contas.getAccessibleContext().setAccessibleName(contas.getText());
        }
            for (String chave : List.of("deposito", "saque", "transferencia")) {
            JButton botao = atalhos.get(chave);
            if (botao != null) botao.setVisible(!administrador);
        }
        ajustarGradeAtalhos();
    }

    void carregandoMovimentacoes() {
        estadoMovimentacoes.setText("Consultando movimentações recentes…");
        estadoMovimentacoes.setForeground(EstiloSwing.SECUNDARIO_CLARO);
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
            estadoMovimentacoes.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        }
    }

    void falhaMovimentacoes() {
        ((java.awt.CardLayout) conteudoRecentes.getLayout()).show(conteudoRecentes, "vazio");
        estadoMovimentacoes.setText("Não foi possível carregar as movimentações recentes.");
        estadoMovimentacoes.setForeground(EstiloSwing.AVISO);
    }

    private JPanel cartoesResumo() {
        JPanel faixa = new JPanel(new java.awt.GridLayout(1, 0, 8, 0));
        faixa.setOpaque(false);
        faixa.add(cartaoResumo("CONTAS", totalContas, "Contas cadastradas"));
        faixa.add(cartaoResumo("SALDO TOTAL", saldoTotal, "Soma dos saldos atuais"));
        faixa.add(cartaoResumo("MOVIMENTAÇÕES", totalMovimentacoes, "Recentes"));
        return faixa;
    }

    private JPanel cartaoResumo(String titulo, JLabel valor, String descricao) {
        JPanel cartao = new JPanel(new BorderLayout(0, 4));
        EstiloSwing.cartaoClaro(cartao);
        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        rotulo.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        valor.setForeground(EstiloSwing.TEXTO_ESCURO);
        JLabel apoio = new JLabel(descricao);
        apoio.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        apoio.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        cartao.add(rotulo, BorderLayout.NORTH);
        cartao.add(valor, BorderLayout.CENTER);
        cartao.add(apoio, BorderLayout.SOUTH);
        return cartao;
    }

    private JPanel cartaoAtalhos() {
        JPanel cartao = new JPanel(new BorderLayout(0, 12));
        EstiloSwing.cartaoClaro(cartao);
        JLabel titulo = new JLabel("Acesso rápido");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titulo.setForeground(EstiloSwing.TEXTO_ESCURO);
        cartao.add(titulo, BorderLayout.NORTH);

        botoesAtalho.setOpaque(false);
        adicionarAtalho("deposito", "Depósito", "deposito");
        adicionarAtalho("saque", "Saque", "saque");
        adicionarAtalho("transferencia", "Transferência", "transferencia");
        adicionarAtalho("contas", "Minhas contas", "contas");
        adicionarAtalho("extrato", "Extrato", "extrato");
        adicionarAtalho("chat", "Assistente", "chat");
        cartao.add(botoesAtalho, BorderLayout.CENTER);
        return cartao;
    }

    private void adicionarAtalho(String chave, String texto, String tela) {
        JButton botao = EstiloSwing.cartaoAcao(texto, "chat".equals(chave) ? "ia" : tela);
        botao.addActionListener(evento -> navegar.accept(tela));
        botoesAtalho.add(botao);
        atalhos.put(chave, botao);
    }

    private void ajustarGradeAtalhos() {
        long visiveis = atalhos.values().stream().filter(JButton::isVisible).count();
        int preferidas = getWidth() >= EstiloSwing.LARGURA_ATALHOS_AMPLA ? 3
                : getWidth() >= 430 ? 2 : 1;
        int colunas = (int) Math.max(1, Math.min(preferidas, visiveis));
        if (colunas == colunasAtalho && visiveis == quantidadeAtalhos) return;
        colunasAtalho = colunas;
        quantidadeAtalhos = (int) visiveis;
        botoesAtalho.removeAll();
        int indice = 0;
        for (JButton atalho : atalhos.values()) {
            if (!atalho.isVisible()) continue;
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = indice % colunas;
            c.gridy = indice / colunas;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.insets = new Insets(0, 0, 8, 8);
            botoesAtalho.add(atalho, c);
            indice++;
        }
        botoesAtalho.revalidate();
        botoesAtalho.repaint();
    }

    @Override public void doLayout() {
        ajustarGradeAtalhos();
        super.doLayout();
    }

    private JPanel cartaoRecentes() {
        JPanel cartao = new JPanel(new BorderLayout(0, 12));
        EstiloSwing.cartaoClaro(cartao);
        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setOpaque(false);
        JLabel rotulo = new JLabel("Movimentações recentes");
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        rotulo.setForeground(EstiloSwing.TEXTO_ESCURO);
        JButton verExtrato = EstiloSwing.botaoCompacto("Abrir extrato", true);
        verExtrato.addActionListener(evento -> navegar.accept("extrato"));
        titulo.add(rotulo, BorderLayout.WEST);
        titulo.add(verExtrato, BorderLayout.EAST);
        cartao.add(titulo, BorderLayout.NORTH);

        EstiloSwing.tabelaClara(tabela);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabela.setFillsViewportHeight(true);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(115);
        tabela.getColumnModel().getColumn(2).setCellRenderer(new RendererValor());
        JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(tabela));
        rolagem.setPreferredSize(new java.awt.Dimension(650, 210));
        rolagem.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA));
        conteudoRecentes.add(rolagem, "dados");
        estadoMovimentacoes.setFont(EstiloSwing.FONTE_CORPO);
        estadoMovimentacoes.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        estadoMovimentacoes.setHorizontalAlignment(JLabel.CENTER);
        estadoMovimentacoes.setBorder(BorderFactory.createEmptyBorder(22, 12, 22, 12));
        JPanel vazio = new JPanel(new BorderLayout());
        vazio.setBackground(EstiloSwing.DESTAQUE_CLARO);
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
                setForeground(tipo.startsWith("Entrada") ? EstiloSwing.SUCESSO_TEXTO
                        : tipo.startsWith("Saída") ? EstiloSwing.ERRO_TEXTO : EstiloSwing.TEXTO_ESCURO);
            }
            setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            return componente;
        }
    }
}
