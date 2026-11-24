package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

final class PainelExtrato extends JPanel {
    private static final String DADOS = "dados";
    private static final String VAZIO = "vazio";
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final JComboBox<Conta> contas = new JComboBox<>();
    private final JSpinner dataInicial = criarSeletorData(LocalDate.of(1970, 1, 1));
    private final JSpinner dataFinal = criarSeletorData(LocalDate.now());
    private final JPanel campoDataInicial = campoData(dataInicial);
    private final JPanel campoDataFinal = campoData(dataFinal);
    private final ModeloTransacoes modelo = new ModeloTransacoes();
    private final Consumer<Conta> consultar;
    private final BiConsumer<Conta, Path> exportar;
    private final JLabel estado = new JLabel("Selecione uma conta e consulte o extrato.");
    private final JPanel cartoesTabela = new JPanel(new CardLayout()) {
        @Override public java.awt.Dimension getPreferredSize() {
            for (java.awt.Component componente : getComponents()) {
                if (componente.isVisible()) return componente.getPreferredSize();
            }
            return new java.awt.Dimension(600, 82);
        }
    };
    private final JPanel corpo = new JPanel();
    private final JPanel cartaoMovimentacoes;
    private JPanel cartaoFiltros;
    private JPanel grupoFiltros;
    private JPanel camposFiltros;
    private List<Transacao> transacoesCarregadas = List.of();
    private java.util.Map<UUID, String> numerosContaPorId = java.util.Map.of();

    PainelExtrato(Consumer<Conta> consultar, BiConsumer<Conta, Path> exportar) {
        super(new BorderLayout(0, 12));
        this.consultar = consultar;
        this.exportar = exportar;
        setOpaque(false);
        add(EstiloSwing.cabecalhoTela("Extrato bancário",
                "Consulte as movimentações registradas e exporte o histórico em arquivo TXT."), BorderLayout.NORTH);

        corpo.setLayout(new javax.swing.BoxLayout(corpo, javax.swing.BoxLayout.Y_AXIS));
        corpo.setOpaque(false);
        JPanel filtros = cartaoSelecao();
        filtros.setAlignmentX(LEFT_ALIGNMENT);
        filtros.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 280));
        corpo.add(filtros);
        corpo.add(javax.swing.Box.createVerticalStrut(12));
        cartaoMovimentacoes = cartaoTabela();
        cartaoMovimentacoes.setAlignmentX(LEFT_ALIGNMENT);
        corpo.add(cartaoMovimentacoes);
        add(corpo, BorderLayout.CENTER);
        ajustarAlturaMovimentacoes(false);
    }

    @Override public void doLayout() {
        if (cartaoFiltros != null) {
            ajustarFiltros(grupoFiltros, camposFiltros,
                    getWidth() >= EstiloSwing.LARGURA_EXTRATO_HORIZONTAL);
            ajustarAlturaFiltros();
        }
        super.doLayout();
    }

    void definirContas(List<Conta> novasContas) {
        numerosContaPorId = novasContas.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Conta::getIdentificador, conta -> conta.getNumeroPublico() > 0
                        ? "Conta " + conta.getNumeroPublicoFormatado() : "Conta principal"));
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
        transacoesCarregadas = List.copyOf(transacoes);
        aplicarPeriodo();
    }

    private void aplicarPeriodo() {
        Conta conta = contaSelecionada();
        List<Transacao> filtradas;
        try {
            filtradas = filtrarPeriodo(transacoesCarregadas, dataEscolhida(dataInicial), dataEscolhida(dataFinal));
        } catch (IllegalArgumentException erro) {
            estado.setText(erro.getMessage());
            estado.setForeground(EstiloSwing.ERRO);
            return;
        }
        modelo.definirTransacoes(conta == null ? null : conta.getIdentificador(), filtradas);
        CardLayout cards = (CardLayout) cartoesTabela.getLayout();
        cards.show(cartoesTabela, filtradas.isEmpty() ? VAZIO : DADOS);
        estado.setText(filtradas.isEmpty() ? "Nenhuma movimentação no período selecionado."
                : "Movimentações no período: " + filtradas.size());
        estado.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        ajustarAlturaMovimentacoes(!filtradas.isEmpty());
    }

    private JPanel cartaoSelecao() {
        JPanel cartao = new JPanel(new BorderLayout(10, 0));
        cartaoFiltros = cartao;
        EstiloSwing.cartaoClaro(cartao);
        EstiloSwing.contasClaras(contas);
        JPanel campos = new JPanel(new java.awt.GridLayout(1, 3, 10, 0));
        camposFiltros = campos;
        campos.setOpaque(false);
        campos.add(campoFiltro("Conta", contas));
        campos.add(campoFiltro("Data inicial", campoDataInicial));
        campos.add(campoFiltro("Data final", campoDataFinal));
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        var atualizar = EstiloSwing.botaoCompacto("Consultar período", true);
        atualizar.addActionListener(evento -> {
            Conta conta = contaSelecionada();
            if (conta == null) {
                DialogoConfirmacao.aviso(this, "Conta necessária", "Selecione uma conta.");
                return;
            }
            try {
                filtrarPeriodo(List.of(), dataEscolhida(dataInicial), dataEscolhida(dataFinal));
                estado.setText("Consultando movimentações do período…");
                estado.setForeground(EstiloSwing.SECUNDARIO_CLARO);
                consultar.accept(conta);
            } catch (IllegalArgumentException erro) {
                DialogoConfirmacao.aviso(this, "Período inválido", erro.getMessage());
                dataInicial.requestFocusInWindow();
            }
        });
        var salvar = EstiloSwing.botaoCompacto("Exportar TXT", false);
        salvar.setToolTipText("Exporta o extrato completo desta conta em arquivo TXT.");
        salvar.addActionListener(evento -> escolherDestino());
        botoes.add(atualizar);
        botoes.add(salvar);
        JPanel grupo = new JPanel(new BorderLayout(0, 9));
        grupoFiltros = grupo;
        grupo.setOpaque(false);
        grupo.add(campos, BorderLayout.CENTER);
        grupo.add(estado, BorderLayout.SOUTH);
        cartao.add(grupo, BorderLayout.NORTH);
        cartao.add(botoes, BorderLayout.SOUTH);
        estado.setFont(EstiloSwing.FONTE_AUXILIAR);
        estado.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        return cartao;
    }

    private void ajustarFiltros(JPanel grupo, JPanel campos, boolean horizontal) {
        java.awt.GridLayout layout = (java.awt.GridLayout) campos.getLayout();
        int colunas = horizontal ? 3 : 1;
        if (layout.getColumns() == colunas) return;
        campos.setLayout(new java.awt.GridLayout(horizontal ? 1 : 0, horizontal ? 3 : 1, 10, 7));
        campos.revalidate();
        grupo.revalidate();
        if (cartaoFiltros != null) cartaoFiltros.revalidate();
        corpo.revalidate();
    }

    static JSpinner criarSeletorData(LocalDate data) {
        Date valorInicial = Date.from(data.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner seletor = new JSpinner(new javax.swing.SpinnerDateModel(
                valorInicial, null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(seletor, "dd/MM/yyyy");
        editor.getFormat().setLenient(false);
        editor.getTextField().setColumns(10);
        editor.getTextField().setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT_OR_REVERT);
        EstiloSwing.campoClaro(editor.getTextField());
        seletor.setEditor(editor);
        for (java.awt.Component componente : seletor.getComponents()) {
            if (componente instanceof JButton) {
                componente.setPreferredSize(new java.awt.Dimension(0, 0));
                componente.setMinimumSize(new java.awt.Dimension(0, 0));
                componente.setMaximumSize(new java.awt.Dimension(0, 0));
                componente.setVisible(false);
            }
        }
        seletor.setPreferredSize(new java.awt.Dimension(142, 38));
        seletor.setMinimumSize(new java.awt.Dimension(126, 36));
        seletor.setBackground(EstiloSwing.FUNDO_CAMPO_CLARO);
        seletor.setOpaque(true);
        seletor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloSwing.BORDA_CLARA),
                BorderFactory.createEmptyBorder(1, 3, 1, 3)));
        seletor.setToolTipText("Use as setas para escolher uma data válida.");
        return seletor;
    }

    private static JPanel campoData(JSpinner seletor) {
        JPanel campo = new JPanel(new BorderLayout(3, 0));
        campo.setOpaque(true);
        campo.setBackground(EstiloSwing.FUNDO_CAMPO_CLARO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloSwing.BORDA_CLARA),
                BorderFactory.createEmptyBorder(1, 5, 1, 3)));
        seletor.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        javax.swing.JFormattedTextField entrada = ((JSpinner.DateEditor) seletor.getEditor()).getTextField();
        entrada.setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 4));
        campo.add(seletor, BorderLayout.CENTER);
        JButton calendario = new JButton(new IconeNavegacao("calendario", EstiloSwing.VINHO));
        calendario.setBackground(EstiloSwing.DESTAQUE_CLARO);
        calendario.setOpaque(true);
        calendario.setBorderPainted(false);
        calendario.setFocusPainted(true);
        calendario.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        calendario.setToolTipText("Abrir calendário para selecionar a data");
        calendario.getAccessibleContext().setAccessibleName("Abrir calendário");
        calendario.setPreferredSize(new java.awt.Dimension(34, 32));
        calendario.addActionListener(evento -> mostrarCalendario(seletor, calendario));
        campo.add(calendario, BorderLayout.EAST);
        entrada.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent evento) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(EstiloSwing.VINHO, 2),
                        BorderFactory.createEmptyBorder(0, 4, 0, 2)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent evento) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(EstiloSwing.BORDA_CLARA),
                        BorderFactory.createEmptyBorder(1, 5, 1, 3)));
            }
        });
        campo.setPreferredSize(new java.awt.Dimension(180, 38));
        campo.setMinimumSize(new java.awt.Dimension(150, 36));
        return campo;
    }

    private static void mostrarCalendario(JSpinner seletor, JButton ancora) {
        LocalDate atual;
        try {
            atual = dataEscolhida(seletor);
        } catch (IllegalArgumentException erro) {
            DialogoConfirmacao.aviso(ancora, "Data inválida", erro.getMessage());
            return;
        }
        final java.time.YearMonth[] mesAtual = {java.time.YearMonth.from(atual)};
        JPopupMenu popup = new JPopupMenu();
        JPanel calendario = new JPanel(new BorderLayout(0, 7));
        calendario.setBackground(EstiloSwing.SUPERFICIE_CLARA);
        calendario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloSwing.BORDA_CLARA),
                BorderFactory.createEmptyBorder(9, 9, 9, 9)));
        Runnable redesenhar = new Runnable() {
            @Override public void run() {
                calendario.removeAll();
                JPanel cabecalho = new JPanel(new BorderLayout(8, 0));
                cabecalho.setOpaque(false);
                JButton anterior = new JButton(new IconeMes(false));
                JButton proximo = new JButton(new IconeMes(true));
                for (JButton controle : List.of(anterior, proximo)) {
                    controle.setPreferredSize(new java.awt.Dimension(30, 28));
                    controle.setBackground(EstiloSwing.SUPERFICIE_CLARA);
                    controle.setOpaque(true);
                    controle.setBorderPainted(false);
                    controle.setFocusPainted(true);
                }
                anterior.setToolTipText("Mês anterior");
                proximo.setToolTipText("Próximo mês");
                JLabel mes = new JLabel(mesAtual[0].format(java.time.format.DateTimeFormatter.ofPattern(
                        "MMMM yyyy", java.util.Locale.forLanguageTag("pt-BR"))), JLabel.CENTER);
                mes.setFont(EstiloSwing.FONTE_AUXILIAR.deriveFont(java.awt.Font.BOLD));
                mes.setForeground(EstiloSwing.TEXTO_ESCURO);
                anterior.addActionListener(e -> { mesAtual[0] = mesAtual[0].minusMonths(1); run(); });
                proximo.addActionListener(e -> { mesAtual[0] = mesAtual[0].plusMonths(1); run(); });
                cabecalho.add(anterior, BorderLayout.WEST);
                cabecalho.add(mes, BorderLayout.CENTER);
                cabecalho.add(proximo, BorderLayout.EAST);
                calendario.add(cabecalho, BorderLayout.NORTH);
                JPanel dias = new JPanel(new java.awt.GridLayout(7, 7, 2, 2));
                dias.setOpaque(false);
                for (String nome : List.of("2ª", "3ª", "4ª", "5ª", "6ª", "Sá", "Do")) {
                    JLabel dia = new JLabel(nome, JLabel.CENTER);
                    dia.setForeground(EstiloSwing.SECUNDARIO_CLARO);
                    dia.setFont(EstiloSwing.FONTE_MICRO);
                    dias.add(dia);
                }
                java.time.LocalDate primeiro = mesAtual[0].atDay(1);
                int deslocamento = primeiro.getDayOfWeek().getValue() - 1;
                for (int i = 0; i < deslocamento; i++) dias.add(new JLabel());
                for (int diaNumero = 1; diaNumero <= mesAtual[0].lengthOfMonth(); diaNumero++) {
                    LocalDate data = mesAtual[0].atDay(diaNumero);
                    JButton dia = EstiloSwing.botaoCompacto(Integer.toString(diaNumero), false);
                    dia.setPreferredSize(new java.awt.Dimension(31, 28));
                    dia.setBackground(data.equals(atual) ? EstiloSwing.DESTAQUE_CLARO : EstiloSwing.SUPERFICIE_CLARA);
                    dia.addActionListener(e -> {
                        aplicarData(seletor, data);
                        popup.setVisible(false);
                    });
                    dias.add(dia);
                }
                while (dias.getComponentCount() < 49) dias.add(new JLabel());
                calendario.add(dias, BorderLayout.CENTER);
                popup.setLayout(new BorderLayout());
                popup.removeAll();
                popup.add(calendario, BorderLayout.CENTER);
                popup.pack();
            }
        };
        redesenhar.run();
        popup.show(ancora, 0, ancora.getHeight());
    }

    private static final class IconeMes implements javax.swing.Icon {
        private final boolean proximo;
        IconeMes(boolean proximo) { this.proximo = proximo; }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
        @Override public void paintIcon(java.awt.Component c, java.awt.Graphics grafico, int x, int y) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) grafico.create();
            g.translate(x, y);
            g.setColor(EstiloSwing.VINHO_ESCURO);
            g.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            int sentido = proximo ? 1 : -1;
            int ponta = proximo ? 12 : 4;
            g.drawLine(ponta, 3, ponta - sentido * 5, 8);
            g.drawLine(ponta - sentido * 5, 8, ponta, 13);
            g.dispose();
        }
    }

    private static JPanel campoFiltro(String titulo, java.awt.Component campo) {
        JPanel grupo = new JPanel(new BorderLayout(0, 5));
        grupo.setOpaque(false);
        grupo.add(EstiloSwing.rotuloCampoClaro(titulo), BorderLayout.NORTH);
        grupo.add(campo, BorderLayout.CENTER);
        return grupo;
    }

    static LocalDate dataEscolhida(JSpinner seletor) {
        try {
            seletor.commitEdit();
        } catch (java.text.ParseException erro) {
            throw new IllegalArgumentException("Informe uma data válida no formato DD/MM/AAAA.");
        }
        return ((Date) seletor.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static void aplicarData(JSpinner seletor, LocalDate data) {
        seletor.setValue(Date.from(data.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    static List<Transacao> filtrarPeriodo(List<Transacao> transacoes, LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) throw new IllegalArgumentException("Informe as datas inicial e final.");
        if (fim.isBefore(inicio)) throw new IllegalArgumentException("A data final deve ser igual ou posterior à inicial.");
        return transacoes.stream().filter(transacao -> {
            LocalDate data = transacao.dataHora().toLocalDate();
            return !data.isBefore(inicio) && !data.isAfter(fim);
        }).toList();
    }

    private void ajustarAlturaMovimentacoes(boolean comRegistros) {
        int altura = cartaoMovimentacoes.getPreferredSize().height;
        cartaoMovimentacoes.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE,
                comRegistros ? Integer.MAX_VALUE : altura));
        corpo.revalidate();
    }

    private void ajustarAlturaFiltros() {
        java.awt.Dimension preferida = cartaoFiltros.getPreferredSize();
        if (cartaoFiltros.getMaximumSize().height != preferida.height) {
            cartaoFiltros.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, preferida.height));
            corpo.revalidate();
        }
    }

    private JPanel cartaoTabela() {
        JPanel cartao = new JPanel(new BorderLayout(0, 14));
        EstiloSwing.cartaoClaro(cartao);
        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setOpaque(false);
        JLabel rotulo = new JLabel("Movimentações da conta");
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        rotulo.setForeground(EstiloSwing.TEXTO_ESCURO);
        titulo.add(rotulo, BorderLayout.WEST);
        cartao.add(titulo, BorderLayout.NORTH);

        JTable tabela = new JTable(modelo);
        EstiloSwing.tabelaClara(tabela);
        DefaultTableCellRenderer rendererMovimentacao = new RendererMovimentacao();
        tabela.getColumnModel().getColumn(1).setCellRenderer(rendererMovimentacao);
        tabela.getColumnModel().getColumn(2).setCellRenderer(new RendererValor());
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(145);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(135);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(95);
        JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(tabela));
        rolagem.setPreferredSize(new java.awt.Dimension(700, 260));
        rolagem.setBackground(EstiloSwing.SUPERFICIE_CLARA);
        rolagem.getViewport().setBackground(EstiloSwing.SUPERFICIE_CLARA);
        rolagem.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA_CLARA));
        cartoesTabela.setOpaque(false);
        cartoesTabela.add(rolagem, DADOS);
        JPanel vazio = new JPanel(new BorderLayout(0, 7));
        vazio.setOpaque(false);
        JLabel iconeVazio = new JLabel(new IconeNavegacao("extrato", EstiloSwing.VINHO), JLabel.CENTER);
        vazio.add(iconeVazio, BorderLayout.NORTH);
        JLabel mensagemVazia = new JLabel("Nenhuma movimentação no período selecionado.", JLabel.CENTER);
        mensagemVazia.setFont(EstiloSwing.FONTE_CORPO);
        mensagemVazia.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        vazio.add(mensagemVazia, BorderLayout.CENTER);
        vazio.setPreferredSize(new java.awt.Dimension(600, 82));
        vazio.setMinimumSize(new java.awt.Dimension(220, 78));
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
        seletor.setSelectedFile(Path.of("extrato-conta-" + conta.getNumeroPublicoFormatado() + ".txt").toFile());
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

    private final class ModeloTransacoes extends AbstractTableModel {
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
                case 3 -> numerosContaPorId.getOrDefault(transacao.contaOrigem(), "Outra conta");
                case 4 -> transacao.contaDestino() == null ? "—"
                        : numerosContaPorId.getOrDefault(transacao.contaDestino(), "Outra conta");
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
                        ? EstiloSwing.SUCESSO_TEXTO : EstiloSwing.ERRO_TEXTO);
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
