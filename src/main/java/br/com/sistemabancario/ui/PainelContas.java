package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

final class PainelContas extends JPanel {
    private static final String LISTA = "lista";
    private static final String VAZIA = "vazia";
    private final ModeloContas modelo = new ModeloContas();
    private final JTable tabela = new JTable(modelo);
    private final JLabel quantidade = new JLabel("0");
    private final JLabel saldoTotal = new JLabel("R$ 0,00");
    private final JPanel conteudoTabela = new JPanel(new CardLayout()) {
        @Override public Dimension getPreferredSize() {
            for (Component componente : getComponents()) {
                if (componente.isVisible()) return componente.getPreferredSize();
            }
            return new Dimension(420, 96);
        }
    };
    private final JPanel corpo = new JPanel(new GridBagLayout());
    private final JLabel tituloTabela = new JLabel("Todas as contas");
    private final JButton[] acoes;
    private final JButton excluir;
    private JButton cadastrar;
    private JPanel cartaoTabela;
    private JPanel painelAcoesTabela;
    private JScrollPane rolagemTabela;
    private final java.util.function.Consumer<Conta> exclusao;
    private final BiConsumer<String, Conta> navegar;

    PainelContas(Runnable atualizar, BiConsumer<String, Conta> navegar,
                 java.util.function.Consumer<Conta> exclusao) {
        super(new BorderLayout(0, 12));
        this.navegar = navegar;
        this.exclusao = exclusao;
        acoes = criarAcoes();
        excluir = EstiloSwing.botao("Excluir conta", false);
        excluir.setForeground(EstiloSwing.ERRO);
        excluir.setEnabled(false);
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Contas", "Acompanhe os saldos e acesse as operações das suas contas."), BorderLayout.NORTH);

        corpo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);
        corpo.add(resumo(), c);
        c.gridy = 1; c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);
        cartaoTabela = cartaoTabela(atualizar);
        corpo.add(cartaoTabela, c);
        c.gridx = 0; c.gridy = 2; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        corpo.add(javax.swing.Box.createGlue(), c);
        add(corpo, BorderLayout.CENTER);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        EstiloSwing.tabelaClara(tabela);
        tabela.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(JTable tabela, Object valor,
                    boolean selecionada, boolean foco, int linha, int coluna) {
                java.awt.Component celula = super.getTableCellRendererComponent(
                        tabela, valor, selecionada, foco, linha, coluna);
                setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                setFont(EstiloSwing.FONTE_CORPO.deriveFont(Font.BOLD));
                if (!selecionada) setForeground(EstiloSwing.ROSA_TEXTO);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 14));
                return celula;
            }
        });
        tabela.getSelectionModel().addListSelectionListener(evento -> atualizarDetalhes());
        atualizarDetalhes();
    }

    void configurarPerfil(boolean administrador) {
        tituloTabela.setText(administrador ? "Todas as contas" : "Minha conta e saldo disponível");
        for (int indice = 0; indice < acoes.length - 1; indice++) {
            acoes[indice].setVisible(!administrador);
        }
        excluir.setVisible(administrador);
        if (cadastrar != null) cadastrar.setVisible(false);
    }

    void definirContas(List<Conta> contas) {
        Conta selecionada = contaSelecionada();
        UUID idSelecionado = selecionada == null ? null : selecionada.getIdentificador();
        modelo.definirContas(contas);
        quantidade.setText(Integer.toString(contas.size()));
        saldoTotal.setText(EstiloSwing.moeda(contas.stream().map(Conta::getSaldo)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)));
        CardLayout cards = (CardLayout) conteudoTabela.getLayout();
        cards.show(conteudoTabela, contas.isEmpty() ? VAZIA : LISTA);
        ajustarAlturaTabela(!contas.isEmpty());
        if (idSelecionado != null) {
            for (int linha = 0; linha < modelo.getRowCount(); linha++) {
                if (modelo.contaNaLinha(linha).getIdentificador().equals(idSelecionado)) {
                    int linhaVisivel = tabela.convertRowIndexToView(linha);
                    if (linhaVisivel >= 0) tabela.setRowSelectionInterval(linhaVisivel, linhaVisivel);
                    break;
                }
            }
        }
        atualizarDetalhes();
    }

    Conta contaSelecionada() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : modelo.contaNaLinha(tabela.convertRowIndexToModel(linha));
    }

    private JPanel resumo() {
        JPanel faixa = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        faixa.setOpaque(false);

        faixa.add(metrica("CONTAS CADASTRADAS", quantidade));
        faixa.add(metrica("SALDO TOTAL DISPONÍVEL", saldoTotal));
        return faixa;
    }

    private static JPanel metrica(String rotulo, JLabel valor) {
        JPanel cartao = new JPanel(new BorderLayout(0, 4));
        EstiloSwing.cartaoClaro(cartao);
        JLabel titulo = new JLabel(rotulo);
        titulo.setFont(EstiloSwing.FONTE_MICRO);
        titulo.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        valor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        valor.setFont(EstiloSwing.FONTE_VALOR);
        valor.setForeground(EstiloSwing.TEXTO_ESCURO);
        cartao.add(titulo, BorderLayout.NORTH);
        cartao.add(valor, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel cartaoTabela(Runnable atualizar) {
        JPanel cartao = new JPanel(new BorderLayout(0, 14));
        EstiloSwing.cartaoClaro(cartao);
        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setOpaque(false);
        tituloTabela.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        tituloTabela.setForeground(EstiloSwing.TEXTO_ESCURO);
        JButton recarregar = EstiloSwing.botaoCompacto("Atualizar lista", false);
        recarregar.addActionListener(evento -> atualizar.run());
        cadastrar = EstiloSwing.botaoCompacto("Novo cliente e conta", true);
        cadastrar.addActionListener(evento -> navegar.accept("cadastro", null));
        JPanel acoesTitulo = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        acoesTitulo.setOpaque(false);
        acoesTitulo.add(recarregar);
        acoesTitulo.add(cadastrar);
        titulo.add(tituloTabela, BorderLayout.NORTH);
        titulo.add(acoesTitulo, BorderLayout.SOUTH);
        cartao.add(titulo, BorderLayout.NORTH);

        rolagemTabela = EstiloSwing.rolagemNatural(new JScrollPane(tabela));
        rolagemTabela.setPreferredSize(new java.awt.Dimension(700, 330));
        rolagemTabela.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA_CLARA));
        conteudoTabela.add(rolagemTabela, LISTA);
        JLabel vazio = new JLabel("Nenhuma conta cadastrada. Use “Cadastrar conta” para começar.", JLabel.CENTER);
        vazio.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        vazio.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        vazio.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(32, 20, 32, 20)));
        vazio.setPreferredSize(new Dimension(600, 96));
        conteudoTabela.add(vazio, VAZIA);
        ((CardLayout) conteudoTabela.getLayout()).show(conteudoTabela, VAZIA);
        cartao.add(conteudoTabela, BorderLayout.CENTER);
        painelAcoesTabela = cartaoAcoes();
        cartao.add(painelAcoesTabela, BorderLayout.SOUTH);
        return cartao;
    }

    private void ajustarAlturaTabela(boolean comContas) {
        if (cartaoTabela == null) return;
        if (rolagemTabela != null && comContas) {
            int linhasVisiveis = Math.min(modelo.getRowCount(), 8);
            int altura = tabela.getTableHeader().getPreferredSize().height
                    + Math.max(1, linhasVisiveis) * tabela.getRowHeight() + 4;
            rolagemTabela.setPreferredSize(new java.awt.Dimension(700, altura));
        }
        GridBagConstraints c = ((GridBagLayout) corpo.getLayout()).getConstraints(cartaoTabela);
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        ((GridBagLayout) corpo.getLayout()).setConstraints(cartaoTabela, c);
        if (painelAcoesTabela != null) painelAcoesTabela.setVisible(comContas);
        corpo.revalidate();
    }

    private JPanel cartaoAcoes() {
        JPanel cartao = new JPanel(new BorderLayout(0, 7));
        cartao.setOpaque(false);
        cartao.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel titulo = new JLabel("Ações da conta selecionada");
        titulo.setFont(EstiloSwing.FONTE_AUXILIAR.deriveFont(Font.BOLD));
        titulo.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        cartao.add(titulo, BorderLayout.NORTH);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        barra.setOpaque(false);
        for (JButton acao : acoes) {
            acao.setPreferredSize(new java.awt.Dimension(112, 34));
            acao.setFont(EstiloSwing.FONTE_AUXILIAR.deriveFont(Font.BOLD));
            barra.add(acao);
        }
        excluir.setPreferredSize(new java.awt.Dimension(112, 34));
        excluir.setFont(EstiloSwing.FONTE_AUXILIAR.deriveFont(Font.BOLD));
        excluir.addActionListener(evento -> {
            Conta conta = contaSelecionada();
            if (conta != null) {
                boolean confirmar = DialogoConfirmacao.confirmar(this, "Excluir conta",
                        "Deseja excluir " + conta.getNomePublico() + " de "
                                + conta.getTitular().getNome() + "?\nEsta ação não poderá ser desfeita.",
                        "Excluir conta", true);
                if (confirmar) exclusao.accept(conta);
            }
        });
        barra.add(excluir);
        cartao.add(barra, BorderLayout.CENTER);
        return cartao;
    }

    private JButton[] criarAcoes() {
        JButton[] botoes = {EstiloSwing.botao("Depositar", true), EstiloSwing.botao("Sacar", false),
                EstiloSwing.botao("Transferir", false), EstiloSwing.botao("Ver extrato", false)};
        String[] destinos = {"deposito", "saque", "transferencia", "extrato"};
        for (int indice = 0; indice < botoes.length; indice++) {
            final String destino = destinos[indice];
            botoes[indice].setEnabled(false);
            botoes[indice].addActionListener(evento -> {
                Conta conta = contaSelecionada();
                if (conta != null) this.navegar.accept(destino, conta);
            });
        }
        return botoes;
    }

    private void atualizarDetalhes() {
        Conta conta = contaSelecionada();
        for (JButton acao : acoes) acao.setEnabled(conta != null);
        excluir.setEnabled(conta != null);
    }

    private static final class ModeloContas extends AbstractTableModel {
        private final String[] colunas = {"Conta", "Titular", "Saldo disponível"};
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
                case 0 -> conta.getNomePublico();
                case 1 -> conta.getTitular().getNome();
                case 2 -> EstiloSwing.moeda(conta.getSaldo());
                default -> throw new IndexOutOfBoundsException(coluna);
            };
        }
    }
}
