package br.com.sistemabancario.ui;

import br.com.sistemabancario.service.ServicoBancario;
import br.com.sistemabancario.service.AutenticacaoService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicProgressBarUI;

/** Ponto de entrada gráfico separado da demonstração de terminal em {@code Main}. */
public final class MainSwing {
    private MainSwing() { }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A interface Swing requer um ambiente gráfico; nenhum banco foi aberto.");
            return;
        }
        SwingUtilities.invokeLater(MainSwing::inicializar);
    }

    private static void inicializar() {
        JFrame carregamento = new JFrame("Sistema Bancário");
        carregamento.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        carregamento.setResizable(false);
        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setBackground(EstiloSwing.FUNDO);
        conteudo.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel cartao = new JPanel(new BorderLayout(14, 0));
        EstiloSwing.cartao(cartao);
        JLabel monograma = new JLabel("SB", SwingConstants.CENTER);
        monograma.setOpaque(true);
        monograma.setBackground(EstiloSwing.VINHO);
        monograma.setForeground(EstiloSwing.TEXTO);
        monograma.setFont(EstiloSwing.FONTE_SECAO);
        monograma.setPreferredSize(new Dimension(46, 46));
        monograma.setBorder(BorderFactory.createLineBorder(EstiloSwing.ROSA_ATIVO));
        JPanel textos = new JPanel(new BorderLayout(0, 7));
        textos.setOpaque(false);
        JLabel titulo = new JLabel("Sistema Bancário");
        titulo.setFont(EstiloSwing.FONTE_SECAO);
        titulo.setForeground(EstiloSwing.TEXTO);
        JLabel mensagem = new JLabel("Preparando seu ambiente financeiro…");
        mensagem.setFont(EstiloSwing.FONTE_AUXILIAR);
        mensagem.setForeground(EstiloSwing.SECUNDARIO);
        JProgressBar progresso = new JProgressBar();
        progresso.setUI(new BasicProgressBarUI());
        progresso.setIndeterminate(true);
        progresso.setForeground(EstiloSwing.ROSA_ATIVO);
        progresso.setBackground(EstiloSwing.VINHO_ESCURO);
        progresso.setBorderPainted(false);
        progresso.setPreferredSize(new Dimension(300, 4));
        JPanel detalhe = new JPanel(new BorderLayout(0, 8));
        detalhe.setOpaque(false);
        detalhe.add(mensagem, BorderLayout.NORTH);
        detalhe.add(progresso, BorderLayout.SOUTH);
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(detalhe, BorderLayout.CENTER);
        cartao.add(monograma, BorderLayout.WEST);
        cartao.add(textos, BorderLayout.CENTER);
        conteudo.add(cartao, BorderLayout.CENTER);
        carregamento.setContentPane(conteudo);
        carregamento.setPreferredSize(new Dimension(430, 146));
        carregamento.pack();
        carregamento.setLocationRelativeTo(null);
        carregamento.setVisible(true);

        new SwingWorker<ContextoAplicacao, Void>() {
            @Override protected ContextoAplicacao doInBackground() {
                ServicoBancario banco = new ServicoBancario();
                return new ContextoAplicacao(banco, new AutenticacaoService(banco));
            }

            @Override protected void done() {
                carregamento.dispose();
                try {
                    ContextoAplicacao contexto = get();
                    new JanelaPrincipal(contexto.banco(), contexto.autenticacao()).iniciar();
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    falhaInicializacao(erro);
                } catch (ExecutionException erro) {
                    falhaInicializacao(erro.getCause());
                } catch (RuntimeException erro) {
                    falhaInicializacao(erro);
                }
            }
        }.execute();
    }

    private record ContextoAplicacao(ServicoBancario banco, AutenticacaoService autenticacao) { }

    private static void falhaInicializacao(Throwable erro) {
        String detalhe = erro == null || erro.getMessage() == null
                ? "Erro sem detalhes disponíveis." : erro.getMessage();
        DialogoConfirmacao.erro(null, "Falha na inicialização",
                "Não foi possível inicializar o banco local. A interface não será aberta.\n\n" + detalhe);
    }
}
