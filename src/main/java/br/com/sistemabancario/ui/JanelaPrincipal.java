package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import br.com.sistemabancario.domain.PerfilAcesso;
import br.com.sistemabancario.service.AutenticacaoService;
import br.com.sistemabancario.service.ServicoBancario;
import br.com.sistemabancario.service.ServicoBancarioAutorizado;
import br.com.sistemabancario.service.SessaoBancaria;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

/** Janela Swing; toda chamada ao serviço é executada em segundo plano. */
public final class JanelaPrincipal extends JFrame {
    private static final String INICIO = "inicio";
    private static final String CONTAS = "contas";
    private static final String CHAT = "chat";
    private static final String OPERACOES = "operacoes";
    private static final Pattern UUID_TEXTO = Pattern.compile(
            "(?i)(?<![0-9a-f])[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(?![0-9a-f])");
    private ControladorBancario controlador;
    private final ServicoBancario banco;
    private final AutenticacaoService autenticacao;
    private SessaoBancaria sessaoAtual;
    private JPanel identidadeCabecalho;
    private JLabel perfilAcesso;
    private final JLabel contextoTela = new JLabel("Visão geral");
    private JButton botaoSair;
    private final PainelAreaRolavel areaCartoes = new PainelAreaRolavel(new CardLayout());
    private final JPanel conteudoInterativo = new JPanel(new BorderLayout());
    private final JPanel conteudoPrincipal = new JPanel(new BorderLayout());
    private final JPanel areaInferior = new JPanel(new BorderLayout());
    private final JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
    private final PainelRaizMobile painelRaiz;
    private final JLabel estado = new JLabel("Preparando interface…");
    private final BarraNavegacaoMobile barraNavegacao;
    private final PainelContas painelContas;
    private final PainelInicio painelInicio;
    private final PainelOperacao painelDeposito;
    private final PainelOperacao painelSaque;
    private final PainelTransferencia painelTransferencia;
    private final PainelOperacoes painelOperacoes;
    private final PainelExtrato painelExtrato;
    private final PainelChat painelChat;
    private final BarraNavegacaoLateral barraLateral;
    private boolean ocupado;
    private int versaoDados;
    private boolean navegacaoLateralAtiva;

    public JanelaPrincipal(ServicoBancario banco, AutenticacaoService autenticacao) {
        super("Sistema Bancário");
        this.banco = java.util.Objects.requireNonNull(banco);
        this.autenticacao = java.util.Objects.requireNonNull(autenticacao);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(360, 620));
        setSize(1280, 820);
        setResizable(true);
        setLocationRelativeTo(null);
        painelContas = new PainelContas(this::carregarContas,
                (destino, conta) -> navegar(destino, conta == null ? null : conta.getIdentificador()),
                this::excluirConta);
        painelInicio = new PainelInicio(destino -> navegar(destino, null));
        painelDeposito = new PainelOperacao(PainelOperacao.Tipo.DEPOSITO, this::depositar, true);
        painelSaque = new PainelOperacao(PainelOperacao.Tipo.SAQUE, this::sacar, true);
        painelTransferencia = new PainelTransferencia(this::transferir, true);
        painelOperacoes = new PainelOperacoes(painelDeposito, painelSaque, painelTransferencia);
        painelExtrato = new PainelExtrato(this::consultarExtrato, this::exportarExtrato);
        painelChat = new PainelChat(new ClienteChatQwenLocal());
        barraNavegacao = new BarraNavegacaoMobile(destino -> navegar(destino, null));
        barraLateral = new BarraNavegacaoLateral(destino -> navegar(destino, null));

        painelRaiz = new PainelRaizMobile(conteudoInterativo, new PainelAcesso.OperacoesAcesso() {
                    @Override public SessaoBancaria autenticar(String login, char[] senha) {
                        return autenticacao.autenticar(login, senha);
                    }
                    @Override public SessaoBancaria registrarCliente(String nome, String login, char[] senha,
                                                                     BigDecimal saldoInicial) {
                        return autenticacao.registrarCliente(nome, login, senha, saldoInicial);
                    }
                    @Override public boolean administradorConfigurado() {
                        return autenticacao.administradorConfigurado();
                    }
                    @Override public SessaoBancaria registrarAdministradorInicial(String nome, String login,
                                                                                   char[] senha) {
                        return autenticacao.registrarAdministradorInicial(nome, login, senha);
                    }
                }, this::ativarSessao);
        setContentPane(painelRaiz);
        montarCabecalho();
        montarAreaPrincipal();
        montarRodape();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent evento) { ajustarNavegacaoResponsiva(); }
        });
        ajustarNavegacaoResponsiva();
    }

    public void iniciar() {
        painelRaiz.mostrarAcesso();
        setVisible(true);
    }

    private void montarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(EstiloSwing.SUPERFICIE);
        JPanel identidade = new JPanel(new BorderLayout(13, 0));
        identidadeCabecalho = identidade;
        identidade.setOpaque(false);
        JLabel monograma = new JLabel("SB", JLabel.CENTER);
        monograma.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        monograma.setForeground(Color.WHITE);
        monograma.setOpaque(true);
        monograma.setBackground(EstiloSwing.VINHO);
        monograma.setPreferredSize(new Dimension(38, 38));
        identidadBorder(monograma);
        JPanel textosMarca = new JPanel(new java.awt.GridLayout(0, 1, 0, 2));
        textosMarca.setOpaque(false);
        JLabel marca = new JLabel("Sistema Bancário");
        marca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        marca.setForeground(EstiloSwing.ROSA_AVERMELHADO);
        JLabel subtitulo = new JLabel("Contas e operações do dia a dia");
        subtitulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        subtitulo.setForeground(EstiloSwing.SECUNDARIO);
        textosMarca.add(marca);
        textosMarca.add(subtitulo);
        identidade.add(monograma, BorderLayout.WEST);
        identidade.add(textosMarca, BorderLayout.CENTER);
        perfilAcesso = new JLabel("LOCAL");
        perfilAcesso.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        perfilAcesso.setForeground(EstiloSwing.ROSA_ATIVO);
        perfilAcesso.setOpaque(true);
        perfilAcesso.setBackground(EstiloSwing.VINHO_ESCURO);
        perfilAcesso.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));
        botaoSair = EstiloSwing.botao("Sair", false);
        botaoSair.setVisible(false);
        botaoSair.addActionListener(evento -> encerrarSessao());
        JPanel acoesCabecalho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoesCabecalho.setOpaque(false);
        acoesCabecalho.add(perfilAcesso);
        acoesCabecalho.add(botaoSair);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        cabecalho.add(identidade, BorderLayout.WEST);
        contextoTela.setFont(EstiloSwing.FONTE_SECAO);
        contextoTela.setForeground(EstiloSwing.TEXTO);
        contextoTela.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        contextoTela.setVisible(false);
        cabecalho.add(contextoTela, BorderLayout.CENTER);
        cabecalho.add(acoesCabecalho, BorderLayout.EAST);
        conteudoPrincipal.add(cabecalho, BorderLayout.NORTH);
    }

    private static void identidadBorder(JLabel monograma) {
        monograma.setBorder(BorderFactory.createLineBorder(EstiloSwing.ROSA_ATIVO));
    }

    private void montarAreaPrincipal() {
        conteudoInterativo.setOpaque(false);
        areaCartoes.setOpaque(false);
        areaCartoes.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        registrarPaginas(areaCartoes, painelInicio, painelContas, painelOperacoes, painelExtrato, painelChat);
        conteudoPrincipal.setOpaque(false);
        instalarAreaRolavel(conteudoPrincipal, areaCartoes);
        conteudoInterativo.add(conteudoPrincipal, BorderLayout.CENTER);
        navegar(INICIO, null);
    }

    static JScrollPane instalarAreaRolavel(JPanel destino, PainelAreaRolavel paginas) {
        JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(paginas,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        destino.add(rolagem, BorderLayout.CENTER);
        return rolagem;
    }

    static void registrarPaginas(PainelAreaRolavel area, Component inicio, Component contas,
                                 Component operacoes, Component extrato, Component chat) {
        area.registrar(INICIO, inicio);
        area.registrar(CONTAS, contas);
        area.registrar(OPERACOES, operacoes);
        area.registrar("extrato", extrato);
        area.registrar(CHAT, chat);
    }

    boolean paginaRegistrada(String pagina) { return areaCartoes.registrada(pagina); }
    String paginaAtual() { return areaCartoes.telaAtual(); }
    boolean paginaVisivel(String pagina) {
        Component painel = areaCartoes.componente(pagina);
        return painel != null && painel.isVisible();
    }
    void navegarParaTeste(String pagina) { navegar(pagina, null); }
    boolean conteudoCentralVisivel() {
        Component centro = ((BorderLayout) conteudoPrincipal.getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        return centro instanceof JScrollPane && centro.isVisible();
    }

    private void montarRodape() {
        rodape.setBackground(EstiloSwing.SUPERFICIE);
        rodape.setOpaque(true);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloSwing.BORDA));
        estado.setForeground(EstiloSwing.SECUNDARIO);
        estado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(2, 12, 2, 12)));
        rodape.add(estado);
        areaInferior.add(rodape, BorderLayout.SOUTH);
        areaInferior.add(barraNavegacao, BorderLayout.CENTER);
    }

    private void ajustarNavegacaoResponsiva() {
        boolean lateral = getWidth() >= EstiloSwing.LARGURA_NAVEGACAO_LATERAL;
        if (lateral == navegacaoLateralAtiva) return;
        navegacaoLateralAtiva = lateral;
        identidadeCabecalho.setVisible(!lateral);
        contextoTela.setVisible(lateral);
        if (lateral) {
            conteudoPrincipal.remove(areaInferior);
            conteudoPrincipal.add(rodape, BorderLayout.SOUTH);
            conteudoInterativo.add(barraLateral, BorderLayout.WEST);
        } else {
            conteudoPrincipal.remove(rodape);
            conteudoPrincipal.add(areaInferior, BorderLayout.SOUTH);
            conteudoInterativo.remove(barraLateral);
        }
        conteudoPrincipal.revalidate();
        conteudoInterativo.revalidate();
        conteudoInterativo.repaint();
    }

    private void navegar(String destino, UUID contaId) {
        String tela = switch (destino) {
            case "deposito", "saque", "transferencia" -> OPERACOES;
            case "cadastro" -> CONTAS;
            default -> destino;
        };
        painelOperacoes.selecionar(destino);
        areaCartoes.mostrar(tela);
        contextoTela.setText(switch (tela) {
            case INICIO -> "Visão geral";
            case CONTAS -> "Contas e saldos";
            case OPERACOES -> "Ações bancárias";
            case "extrato" -> "Extrato bancário";
            case CHAT -> "Assistente do sistema";
            default -> "Sistema Bancário";
        });
        barraNavegacao.selecionar(destino);
        barraLateral.selecionar(destino);
        if (contaId != null) {
            painelDeposito.selecionar(contaId);
            painelSaque.selecionar(contaId);
            painelTransferencia.selecionarOrigem(contaId);
            painelExtrato.selecionar(contaId);
        }
    }

    private void ativarSessao(SessaoBancaria sessao) {
        limparDadosPrivados();
        sessaoAtual = sessao;
        controlador = new ControladorBancario(new ServicoBancarioAutorizado(banco, autenticacao, sessao));
        boolean administrador = autenticacao.perfil(sessao) == PerfilAcesso.ADMINISTRADOR;
        perfilAcesso.setText(administrador ? "ADMINISTRADOR" : "CLIENTE");
        botaoSair.setVisible(true);
        barraNavegacao.configurarPerfil(administrador);
        barraLateral.configurarPerfil(administrador);
        painelContas.configurarPerfil(administrador);
        painelInicio.configurarPerfil(administrador);
        carregarContas();
    }

    private void encerrarSessao() {
        if (ocupado) return;
        if (sessaoAtual != null) autenticacao.encerrar(sessaoAtual);
        sessaoAtual = null;
        controlador = null;
        limparDadosPrivados();
        botaoSair.setVisible(false);
        perfilAcesso.setText("LOCAL");
        painelRaiz.mostrarAcesso();
    }

    private void limparDadosPrivados() {
        versaoDados++;
        List<Conta> semContas = List.of();
        painelContas.definirContas(semContas);
        painelInicio.definirContas(semContas);
        painelInicio.definirTransacoes(List.of());
        painelDeposito.definirContas(semContas);
        painelSaque.definirContas(semContas);
        painelTransferencia.definirContas(semContas);
        painelTransferencia.operacaoConcluida();
        painelExtrato.definirContas(semContas);
        painelExtrato.definirTransacoes(List.of());
    }

    private void carregarContas() {
        executarConsulta("Carregando contas…", controlador::listarContas, this::definirContas);
    }

    private void definirContas(List<Conta> contas) {
        int versao = ++versaoDados;
        painelContas.definirContas(contas);
        painelInicio.definirContas(contas);
        painelDeposito.definirContas(contas);
        painelSaque.definirContas(contas);
        painelTransferencia.definirContas(contas);
        painelExtrato.definirContas(contas);
        definirEstado(contas.isEmpty() ? "Nenhuma conta cadastrada." : "Contas atualizadas: " + contas.size(),
                EstiloSwing.SECUNDARIO);
        carregarMovimentacoesInicio(contas, versao);
    }

    private void carregarMovimentacoesInicio(List<Conta> contas, int versao) {
        if (contas.isEmpty()) {
            painelInicio.definirTransacoes(List.of());
            return;
        }
        painelInicio.carregandoMovimentacoes();
        new SwingWorker<List<Transacao>, Void>() {
            @Override protected List<Transacao> doInBackground() {
                return controlador.consultarMovimentacoesRecentes(6);
            }

            @Override protected void done() {
                if (versao != versaoDados) return;
                try {
                    painelInicio.definirTransacoes(get());
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    painelInicio.falhaMovimentacoes();
                } catch (ExecutionException erro) {
                    painelInicio.falhaMovimentacoes();
                }
            }
        }.execute();
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

    private void transferir(Conta origem, long destinoNumero, BigDecimal valor) {
        executarOperacao("Processando transferência…",
                () -> controlador.transferirPorNumeroPublico(origem.getIdentificador(), destinoNumero, valor),
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
        definirEstado(carregando, EstiloSwing.VINHO);
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
