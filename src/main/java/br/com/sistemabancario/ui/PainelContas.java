package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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

final class PainelContas extends JPanel {
    private static final String LISTA = "lista";
    private static final String VAZIA = "vazia";
    private final ModeloContas modelo = new ModeloContas();
    private final JTable tabela = new JTable(modelo);
    private final JLabel quantidade = new JLabel("0");
    private final JLabel titularSelecionado = new JLabel("Nenhuma conta selecionada");
    private final JLabel saldoSelecionado = new JLabel("Escolha uma conta na tabela para ver os detalhes.");
    private final JPanel conteudoTabela = new JPanel(new CardLayout());
    private final JButton[] acoes;
    private final JButton excluir;
    private final java.util.function.Consumer<Conta> exclusao;
    private final BiConsumer<String, Conta> navegar;

    PainelContas(Runnable atualizar, BiConsumer<String, Conta> navegar,
                 java.util.function.Consumer<Conta> exclusao) {
        super(new BorderLayout(0, 22));
        this.navegar = navegar;
        this.exclusao = exclusao;
        acoes = criarAcoes();
        excluir = EstiloSwing.botao("Excluir conta", false);
        excluir.setForeground(EstiloSwing.ERRO);
        excluir.setEnabled(false);
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Contas", "Acompanhe os saldos e acesse as operações das suas contas."), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new GridBagLayout());
        corpo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 17, 0);
        corpo.add(resumo(), c);
        c.gridy = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 16, 0);
        corpo.add(cartaoTabela(atualizar), c);
        c.gridy = 2; c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        corpo.add(cartaoAcoes(), c);
        add(corpo, BorderLayout.CENTER);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        EstiloSwing.tabela(tabela);
        tabela.getSelectionModel().addListSelectionListener(evento -> atualizarDetalhes());
        atualizarDetalhes();
    }

    void definirContas(List<Conta> contas) {
        Conta selecionada = contaSelecionada();
        UUID idSelecionado = selecionada == null ? null : selecionada.getIdentificador();
        modelo.definirContas(contas);
        quantidade.setText(Integer.toString(contas.size()));
        CardLayout cards = (CardLayout) conteudoTabela.getLayout();
        cards.show(conteudoTabela, contas.isEmpty() ? VAZIA : LISTA);
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
        JPanel faixa = new JPanel(new java.awt.GridLayout(1, 2, 16, 0));
        faixa.setOpaque(false);

        JPanel cartaoQuantidade = new JPanel(new BorderLayout(0, 7));
        EstiloSwing.cartao(cartaoQuantidade);
        JLabel rotuloQuantidade = new JLabel("CONTAS CADASTRADAS");
        rotuloQuantidade.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        rotuloQuantidade.setForeground(EstiloSwing.SECUNDARIO);
        quantidade.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 27));
        quantidade.setForeground(EstiloSwing.MARINHO);
        cartaoQuantidade.add(rotuloQuantidade, BorderLayout.NORTH);
        cartaoQuantidade.add(quantidade, BorderLayout.CENTER);
        faixa.add(cartaoQuantidade);

        JPanel cartaoSelecionada = new JPanel(new java.awt.GridLayout(0, 1, 0, 5));
        EstiloSwing.cartao(cartaoSelecionada);
        JLabel rotuloSelecao = new JLabel("DETALHES DA SELEÇÃO");
        rotuloSelecao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        rotuloSelecao.setForeground(EstiloSwing.SECUNDARIO);
        titularSelecionado.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titularSelecionado.setForeground(EstiloSwing.TEXTO);
        saldoSelecionado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        saldoSelecionado.setForeground(EstiloSwing.SECUNDARIO);
        cartaoSelecionada.add(rotuloSelecao);
        cartaoSelecionada.add(titularSelecionado);
        cartaoSelecionada.add(saldoSelecionado);
        faixa.add(cartaoSelecionada);
        return faixa;
    }

    private JPanel cartaoTabela(Runnable atualizar) {
        JPanel cartao = new JPanel(new BorderLayout(0, 14));
        EstiloSwing.cartao(cartao);
        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setOpaque(false);
        JLabel rotulo = new JLabel("Todas as contas");
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        rotulo.setForeground(EstiloSwing.TEXTO);
        JButton recarregar = EstiloSwing.botao("Atualizar lista", false);
        recarregar.addActionListener(evento -> atualizar.run());
        titulo.add(rotulo, BorderLayout.WEST);
        titulo.add(recarregar, BorderLayout.EAST);
        cartao.add(titulo, BorderLayout.NORTH);

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA));
        conteudoTabela.add(rolagem, LISTA);
        JLabel vazio = new JLabel("Nenhuma conta cadastrada. Use “Cadastrar conta” para começar.", JLabel.CENTER);
        vazio.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        vazio.setForeground(EstiloSwing.SECUNDARIO);
        vazio.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(32, 20, 32, 20)));
        conteudoTabela.add(vazio, VAZIA);
        cartao.add(conteudoTabela, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel cartaoAcoes() {
        JPanel cartao = new JPanel(new BorderLayout(0, 12));
        EstiloSwing.cartao(cartao);
        JLabel titulo = new JLabel("Ações da conta selecionada");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titulo.setForeground(EstiloSwing.TEXTO);
        cartao.add(titulo, BorderLayout.NORTH);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        barra.setOpaque(false);
        for (JButton acao : acoes) barra.add(acao);
        excluir.addActionListener(evento -> {
            Conta conta = contaSelecionada();
            if (conta != null) {
                boolean confirmar = DialogoConfirmacao.confirmar(this, "Excluir conta",
                        "Deseja excluir a conta de " + conta.getTitular().getNome() + " ("
                                + conta.getIdentificadorCurto() + ")?\nEsta ação não poderá ser desfeita.",
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
        if (conta == null) {
            titularSelecionado.setText("Nenhuma conta selecionada");
            saldoSelecionado.setText("Selecione uma linha da tabela para ver os detalhes.");
        } else {
            titularSelecionado.setText(conta.getTitular().getNome() + " · " + conta.getIdentificadorCurto());
            saldoSelecionado.setText("Saldo atual: " + EstiloSwing.moeda(conta.getSaldo()));
        }
    }

    private static final class ModeloContas extends AbstractTableModel {
        private final String[] colunas = {"Titular", "Identificador da conta", "Saldo disponível"};
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
                case 1 -> conta.getIdentificadorCurto();
                case 2 -> EstiloSwing.moeda(conta.getSaldo());
                default -> throw new IndexOutOfBoundsException(coluna);
            };
        }
    }
}
