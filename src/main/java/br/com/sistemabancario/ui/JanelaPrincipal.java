package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/** Janela Swing; toda chamada ao serviço é executada em segundo plano. */
public final class JanelaPrincipal extends JFrame {
    private static final String CONTAS = "contas";
    private static final Pattern UUID_TEXTO = Pattern.compile(
            "(?i)(?<![0-9a-f])[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(?![0-9a-f])");
    private final ControladorBancario controlador;
    private final JPanel areaCartoes = new JPanel(new CardLayout());
    private final JPanel conteudoInterativo = new JPanel(new BorderLayout());
    private final JLabel estado = new JLabel("Preparando interface…");
    private final Map<String, JButton> botoesNavegacao = new LinkedHashMap<>();
    private final PainelContas painelContas;
    private final PainelCadastroConta painelCadastro;
    private final PainelOperacao painelDeposito;
    private final PainelOperacao painelSaque;
    private final PainelTransferencia painelTransferencia;
    private final PainelExtrato painelExtrato;
    private boolean ocupado;

    public JanelaPrincipal(ControladorBancario controlador) {
        super("Sistema Bancário");
        this.controlador = controlador;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1280, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(EstiloSwing.FUNDO);
        getContentPane().setLayout(new BorderLayout());

        painelContas = new PainelContas(this::carregarContas,
                (destino, conta) -> navegar(destino, conta.getIdentificador()), this::excluirConta);
        painelCadastro = new PainelCadastroConta((nome, saldo) -> cadastrarConta(nome, saldo));
        painelDeposito = new PainelOperacao(PainelOperacao.Tipo.DEPOSITO, this::depositar);
        painelSaque = new PainelOperacao(PainelOperacao.Tipo.SAQUE, this::sacar);
        painelTransferencia = new PainelTransferencia(this::transferir);
        painelExtrato = new PainelExtrato(this::consultarExtrato, this::exportarExtrato);

        montarCabecalho();
        montarAreaPrincipal();
        montarRodape();
        getContentPane().add(conteudoInterativo, BorderLayout.CENTER);
    }

    public void iniciar() {
        setVisible(true);
        carregarContas();
    }

    private void montarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Color.WHITE);
        JPanel identidade = new JPanel(new java.awt.GridLayout(0, 1, 0, 2));
        identidade.setOpaque(false);
        JLabel marca = new JLabel("Sistema Bancário");
        marca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
        marca.setForeground(EstiloSwing.MARINHO);
        JLabel subtitulo = new JLabel("Contas e operações do dia a dia");
        subtitulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitulo.setForeground(EstiloSwing.SECUNDARIO);
        identidade.add(marca);
        identidade.add(subtitulo);
        JLabel ambiente = new JLabel("AMBIENTE LOCAL");
        ambiente.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        ambiente.setForeground(new Color(37, 110, 83));
        ambiente.setOpaque(true);
        ambiente.setBackground(new Color(231, 245, 238));
        ambiente.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 231, 217)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(14, 26, 14, 26)));
        cabecalho.add(identidade, BorderLayout.WEST);
        cabecalho.add(ambiente, BorderLayout.EAST);
        getContentPane().add(cabecalho, BorderLayout.NORTH);
    }

    private void montarAreaPrincipal() {
        conteudoInterativo.setOpaque(false);
        JPanel navegacao = new JPanel();
        navegacao.setLayout(new BoxLayout(navegacao, BoxLayout.Y_AXIS));
        navegacao.setBackground(EstiloSwing.MARINHO);
        navegacao.setBorder(BorderFactory.createEmptyBorder(24, 14, 18, 14));
        navegacao.setPreferredSize(new Dimension(232, 0));
        JLabel menu = new JLabel("MENU PRINCIPAL");
        menu.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        menu.setForeground(new Color(180, 195, 216));
        menu.setBorder(BorderFactory.createEmptyBorder(2, 10, 14, 0));
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);
        navegacao.add(menu);
        adicionarNavegacao(navegacao, "Contas", CONTAS);
        navegacao.add(Box.createVerticalStrut(6));
        adicionarNavegacao(navegacao, "Cadastrar conta", "cadastro");
        navegacao.add(Box.createVerticalStrut(6));
        adicionarNavegacao(navegacao, "Depósito", "deposito");
        navegacao.add(Box.createVerticalStrut(6));
        adicionarNavegacao(navegacao, "Saque", "saque");
        navegacao.add(Box.createVerticalStrut(6));
        adicionarNavegacao(navegacao, "Transferência", "transferencia");
        navegacao.add(Box.createVerticalStrut(6));
        adicionarNavegacao(navegacao, "Extrato", "extrato");
        navegacao.add(Box.createVerticalGlue());
        JLabel rodapeNav = new JLabel("SISTEMA BANCÁRIO  •  LOCAL");
        rodapeNav.setForeground(new Color(180, 195, 216));
        rodapeNav.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        rodapeNav.setBorder(BorderFactory.createEmptyBorder(16, 10, 2, 0));
        rodapeNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        navegacao.add(rodapeNav);

        areaCartoes.setOpaque(false);
        areaCartoes.setBorder(BorderFactory.createEmptyBorder(30, 34, 28, 34));
        areaCartoes.add(painelContas, CONTAS);
        areaCartoes.add(painelCadastro, "cadastro");
        areaCartoes.add(painelDeposito, "deposito");
        areaCartoes.add(painelSaque, "saque");
        areaCartoes.add(painelTransferencia, "transferencia");
        areaCartoes.add(painelExtrato, "extrato");
        conteudoInterativo.add(navegacao, BorderLayout.WEST);
        conteudoInterativo.add(areaCartoes, BorderLayout.CENTER);
        navegar(CONTAS, null);
    }

    private void adicionarNavegacao(JPanel navegacao, String rotulo, String cartao) {
        JButton botao = new JButton(rotulo);
        botao.setHorizontalAlignment(JButton.LEFT);
        botao.setMargin(new Insets(0, 0, 0, 0));
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        botao.setForeground(new Color(235, 241, 249));
        botao.setBackground(EstiloSwing.MARINHO);
        botao.setFocusPainted(false);
        botao.setBorderPainted(true);
        botao.setOpaque(true);
        botao.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        botao.setAlignmentX(Component.LEFT_ALIGNMENT);
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        botao.setPreferredSize(new Dimension(200, 50));
        botao.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 12));
        botao.addActionListener(evento -> navegar(cartao, null));
        botoesNavegacao.put(cartao, botao);
        navegacao.add(botao);
    }

    private void montarRodape() {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloSwing.BORDA));
        estado.setForeground(EstiloSwing.SECUNDARIO);
        estado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(2, 12, 2, 12)));
        rodape.add(estado);
        getContentPane().add(rodape, BorderLayout.SOUTH);
    }

    private void navegar(String destino, UUID contaId) {
        CardLayout cards = (CardLayout) areaCartoes.getLayout();
        cards.show(areaCartoes, destino);
        botoesNavegacao.forEach((chave, botao) -> {
            boolean selecionado = chave.equals(destino);
            botao.setBackground(selecionado ? EstiloSwing.AZUL_ATIVO : EstiloSwing.MARINHO);
            botao.setForeground(selecionado ? Color.WHITE : new Color(220, 230, 243));
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0,
                            selecionado ? new Color(117, 177, 239) : EstiloSwing.MARINHO),
                    BorderFactory.createEmptyBorder(0, 13, 0, 12)));
        });
        if (contaId != null) {
            painelDeposito.selecionar(contaId);
            painelSaque.selecionar(contaId);
            painelTransferencia.selecionarOrigem(contaId);
            painelExtrato.selecionar(contaId);
        }
    }

    private void carregarContas() {
        executarConsulta("Carregando contas…", controlador::listarContas, this::definirContas);
    }

    private void definirContas(List<Conta> contas) {
        painelContas.definirContas(contas);
        painelDeposito.definirContas(contas);
        painelSaque.definirContas(contas);
        painelTransferencia.definirContas(contas);
        painelExtrato.definirContas(contas);
        definirEstado(contas.isEmpty() ? "Nenhuma conta cadastrada." : "Contas atualizadas: " + contas.size(),
                EstiloSwing.SECUNDARIO);
    }

    private void cadastrarConta(String nome, BigDecimal saldoInicial) {
        executarOperacao("Cadastrando conta…",
                () -> controlador.criarConta(new Cliente(nome), saldoInicial),
                conta -> "Conta criada para " + conta.getTitular().getNome() + " (" + conta.getIdentificadorCurto()
                        + ") com saldo inicial de " + EstiloSwing.moeda(conta.getSaldo()),
                conta -> painelCadastro.cadastroConcluido());
    }

    private void excluirConta(Conta conta) {
        executarOperacao("Excluindo conta…", () -> {
            controlador.excluirConta(conta.getIdentificador());
            return conta;
        }, removida -> "Conta " + removida.getIdentificadorCurto() + " excluída com sucesso.",
                removida -> { });
    }

    private void depositar(Conta conta, BigDecimal valor) {
        executarOperacao("Processando depósito…",
                () -> controlador.depositar(conta.getIdentificador(), valor),
                this::mensagemTransacao, transacao -> painelDeposito.operacaoConcluida());
    }

    private void sacar(Conta conta, BigDecimal valor) {
        executarOperacao("Processando saque…",
                () -> controlador.sacar(conta.getIdentificador(), valor),
                this::mensagemTransacao, transacao -> painelSaque.operacaoConcluida());
    }

    private void transferir(Conta origem, Conta destino, BigDecimal valor) {
        executarOperacao("Processando transferência…",
                () -> controlador.transferir(origem.getIdentificador(), destino.getIdentificador(), valor),
                this::mensagemTransacao, transacao -> painelTransferencia.operacaoConcluida());
    }

    private void consultarExtrato(Conta conta) {
        executarConsulta("Consultando extrato…",
                () -> controlador.consultarExtrato(conta.getIdentificador()), painelExtrato::definirTransacoes);
    }

    private void exportarExtrato(Conta conta, Path arquivo) {
        executarConsulta("Exportando extrato…",
                () -> controlador.exportarExtrato(conta.getIdentificador(), arquivo),
                caminho -> DialogoConfirmacao.sucesso(this, "Exportação concluída",
                        "Extrato exportado para:\n" + caminho));
    }

    private String mensagemTransacao(Transacao transacao) {
        return transacao.tipo() + " concluído. Valor: " + EstiloSwing.moeda(transacao.valor())
                + "\nIdentificador da transação: "
                + Conta.formatarIdentificador(transacao.identificador());
    }

    private <T> void executarOperacao(String carregando, Callable<T> tarefa,
                                      Function<T, String> mensagemSucesso, Consumer<T> aposSucesso) {
        executar(carregando, () -> {
            T resultado = tarefa.call();
            try {
                return new ResultadoOperacao<>(resultado, controlador.listarContas(), null);
            } catch (RuntimeException falhaAtualizacao) {
                return new ResultadoOperacao<>(resultado, null, falhaAtualizacao);
            }
        }, resultado -> {
            aposSucesso.accept(resultado.valor());
            if (resultado.contas() != null) definirContas(resultado.contas());
            String mensagem = mensagemSucesso.apply(resultado.valor());
            if (resultado.falhaAtualizacao() != null) {
                mensagem += "\n\nA operação foi concluída, mas não foi possível atualizar a listagem: "
                        + mensagemErro(resultado.falhaAtualizacao());
                definirEstado("Operação concluída; atualização das contas falhou.", EstiloSwing.AVISO);
            } else {
                definirEstado("Operação concluída com sucesso.", EstiloSwing.SUCESSO);
            }
            if (resultado.falhaAtualizacao() == null) {
                DialogoConfirmacao.sucesso(this, "Operação concluída", mensagem);
            } else {
                DialogoConfirmacao.aviso(this, "Operação concluída com aviso", mensagem);
            }
        });
    }

    private <T> void executarConsulta(String carregando, Callable<T> tarefa, Consumer<T> aoConcluir) {
        executar(carregando, tarefa, aoConcluir);
    }

    private <T> void executar(String carregando, Callable<T> tarefa, Consumer<T> aoConcluir) {
        if (ocupado) return;
        ocupado = true;
        definirEstado(carregando, EstiloSwing.AZUL);
        definirInteracao(conteudoInterativo, false);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarefa.call(); }

            @Override protected void done() {
                try {
                    T resultado = get();
                    aoConcluir.accept(resultado);
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    mostrarFalha("A operação foi interrompida.", erro);
                } catch (ExecutionException erro) {
                    mostrarFalha("Não foi possível concluir a operação.", erro.getCause());
                } catch (RuntimeException erro) {
                    mostrarFalha("Não foi possível atualizar a interface.", erro);
                } finally {
                    ocupado = false;
                    definirInteracao(conteudoInterativo, true);
                }
            }
        }.execute();
    }

    private void mostrarFalha(String titulo, Throwable causa) {
        String mensagem = mensagemErro(causa);
        definirEstado("Falha: " + mensagem, EstiloSwing.ERRO);
        DialogoConfirmacao.erro(this, "Falha na operação", titulo + "\n\n" + mensagem);
    }

    private static String mensagemErro(Throwable erro) {
        if (erro == null) return "Erro sem detalhes disponíveis.";
        String mensagem = formatarIds(erro.getMessage());
        if (mensagem == null || mensagem.isBlank()) mensagem = erro.getClass().getSimpleName();
        Throwable causa = erro.getCause();
        if (causa != null && causa != erro) {
            String detalhe = formatarIds(causa.getMessage());
            if (detalhe == null || detalhe.isBlank()) detalhe = causa.getClass().getSimpleName();
            return mensagem + " — causa: " + detalhe;
        }
        return mensagem;
    }

    private static String formatarIds(String texto) {
        if (texto == null) return null;
        Matcher encontrados = UUID_TEXTO.matcher(texto);
        StringBuffer resultado = new StringBuffer();
        while (encontrados.find()) {
            String curto = Conta.formatarIdentificador(UUID.fromString(encontrados.group()));
            encontrados.appendReplacement(resultado, Matcher.quoteReplacement(curto));
        }
        encontrados.appendTail(resultado);
        return resultado.toString();
    }

    private void definirEstado(String mensagem, Color cor) {
        estado.setText(mensagem);
        estado.setForeground(cor);
    }

    private static void definirInteracao(Component componente, boolean habilitado) {
        componente.setEnabled(habilitado);
        if (componente instanceof java.awt.Container container) {
            for (Component filho : container.getComponents()) definirInteracao(filho, habilitado);
        }
    }

    private record ResultadoOperacao<T>(T valor, List<Conta> contas, RuntimeException falhaAtualizacao) { }
}
