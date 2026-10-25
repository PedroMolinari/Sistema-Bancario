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
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/** Janela Swing; toda chamada ao serviço é executada em segundo plano. */
public final class JanelaPrincipal extends JFrame {
    private static final String CONTAS = "contas";
    private final ControladorBancario controlador;
    private final JPanel areaCartoes = new JPanel(new CardLayout());
    private final JPanel conteudoInterativo = new JPanel(new BorderLayout());
    private final JLabel estado = new JLabel("Preparando interface…");
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
        setMinimumSize(new Dimension(940, 620));
        setSize(1160, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(EstiloSwing.FUNDO);
        getContentPane().setLayout(new BorderLayout());

        painelContas = new PainelContas(this::carregarContas,
                (destino, conta) -> navegar(destino, conta.getIdentificador()));
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
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(14, 22, 14, 22)));
        JLabel marca = new JLabel("Sistema Bancário");
        marca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        marca.setForeground(EstiloSwing.MARINHO);
        JLabel ambiente = new JLabel("AMBIENTE LOCAL");
        ambiente.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        ambiente.setForeground(EstiloSwing.SECUNDARIO);
        cabecalho.add(marca, BorderLayout.WEST);
        cabecalho.add(ambiente, BorderLayout.EAST);
        getContentPane().add(cabecalho, BorderLayout.NORTH);
    }

    private void montarAreaPrincipal() {
        conteudoInterativo.setOpaque(false);
        JPanel navegacao = new JPanel(new GridLayout(0, 1, 0, 6));
        navegacao.setBackground(EstiloSwing.MARINHO);
        navegacao.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
        navegacao.setPreferredSize(new Dimension(205, 0));
        adicionarNavegacao(navegacao, "Contas", CONTAS);
        adicionarNavegacao(navegacao, "Cadastrar conta", "cadastro");
        adicionarNavegacao(navegacao, "Depósito", "deposito");
        adicionarNavegacao(navegacao, "Saque", "saque");
        adicionarNavegacao(navegacao, "Transferência", "transferencia");
        adicionarNavegacao(navegacao, "Extrato", "extrato");

        areaCartoes.setOpaque(false);
        areaCartoes.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        areaCartoes.add(painelContas, CONTAS);
        areaCartoes.add(painelCadastro, "cadastro");
        areaCartoes.add(painelDeposito, "deposito");
        areaCartoes.add(painelSaque, "saque");
        areaCartoes.add(painelTransferencia, "transferencia");
        areaCartoes.add(painelExtrato, "extrato");
        conteudoInterativo.add(navegacao, BorderLayout.WEST);
        conteudoInterativo.add(areaCartoes, BorderLayout.CENTER);
    }

    private void adicionarNavegacao(JPanel navegacao, String rotulo, String cartao) {
        JButton botao = new JButton(rotulo);
        botao.setHorizontalAlignment(JButton.LEFT);
        botao.setMargin(new Insets(10, 12, 10, 12));
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        botao.setForeground(Color.WHITE);
        botao.setBackground(EstiloSwing.MARINHO);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(true);
        botao.addActionListener(evento -> navegar(cartao, null));
        navegacao.add(botao);
    }

    private void montarRodape() {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloSwing.BORDA));
        estado.setForeground(EstiloSwing.SECUNDARIO);
        rodape.add(estado);
        getContentPane().add(rodape, BorderLayout.SOUTH);
    }

    private void navegar(String destino, UUID contaId) {
        CardLayout cards = (CardLayout) areaCartoes.getLayout();
        cards.show(areaCartoes, destino);
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
        estado.setText(contas.isEmpty() ? "Nenhuma conta cadastrada." : "Contas atualizadas: " + contas.size());
    }

    private void cadastrarConta(String nome, BigDecimal saldoInicial) {
        executarOperacao("Cadastrando conta…",
                () -> controlador.criarConta(new Cliente(nome), saldoInicial),
                conta -> "Conta criada para " + conta.getTitular().getNome() + " (" + conta.getIdentificador()
                        + ") com saldo inicial de " + EstiloSwing.moeda(conta.getSaldo()),
                conta -> painelCadastro.cadastroConcluido());
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
                caminho -> JOptionPane.showMessageDialog(this, "Extrato exportado para:\n" + caminho,
                        "Exportação concluída", JOptionPane.INFORMATION_MESSAGE));
    }

    private String mensagemTransacao(Transacao transacao) {
        return transacao.tipo() + " concluído. Valor: " + EstiloSwing.moeda(transacao.valor())
                + "\nIdentificador da transação: " + transacao.identificador();
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
                estado.setText("Operação concluída; atualização das contas falhou.");
            } else {
                estado.setText("Operação concluída com sucesso.");
            }
            JOptionPane.showMessageDialog(this, mensagem, "Operação concluída",
                    resultado.falhaAtualizacao() == null ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        });
    }

    private <T> void executarConsulta(String carregando, Callable<T> tarefa, Consumer<T> aoConcluir) {
        executar(carregando, tarefa, aoConcluir);
    }

    private <T> void executar(String carregando, Callable<T> tarefa, Consumer<T> aoConcluir) {
        if (ocupado) return;
        ocupado = true;
        estado.setText(carregando);
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
        estado.setText("Falha: " + mensagem);
        JOptionPane.showMessageDialog(this, titulo + "\n\n" + mensagem,
                "Falha na operação", JOptionPane.ERROR_MESSAGE);
    }

    private static String mensagemErro(Throwable erro) {
        if (erro == null) return "Erro sem detalhes disponíveis.";
        String mensagem = erro.getMessage();
        if (mensagem == null || mensagem.isBlank()) mensagem = erro.getClass().getSimpleName();
        Throwable causa = erro.getCause();
        if (causa != null && causa != erro) {
            String detalhe = causa.getMessage();
            if (detalhe == null || detalhe.isBlank()) detalhe = causa.getClass().getSimpleName();
            return mensagem + " — causa: " + detalhe;
        }
        return mensagem;
    }

    private static void definirInteracao(Component componente, boolean habilitado) {
        componente.setEnabled(habilitado);
        if (componente instanceof java.awt.Container container) {
            for (Component filho : container.getComponents()) definirInteracao(filho, habilitado);
        }
    }

    private record ResultadoOperacao<T>(T valor, List<Conta> contas, RuntimeException falhaAtualizacao) { }
}
