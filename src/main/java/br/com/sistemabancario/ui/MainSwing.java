package br.com.sistemabancario.ui;

import br.com.sistemabancario.service.ServicoBancario;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

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
        JPanel conteudo = new JPanel(new BorderLayout(0, 10));
        conteudo.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        JLabel titulo = new JLabel("Sistema Bancário", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 19f));
        JLabel mensagem = new JLabel("Conectando ao banco local…", SwingConstants.CENTER);
        conteudo.add(titulo, BorderLayout.NORTH);
        conteudo.add(mensagem, BorderLayout.CENTER);
        carregamento.setContentPane(conteudo);
        carregamento.setPreferredSize(new Dimension(360, 130));
        carregamento.pack();
        carregamento.setLocationRelativeTo(null);
        carregamento.setVisible(true);

        new SwingWorker<ServicoBancario, Void>() {
            @Override protected ServicoBancario doInBackground() {
                return new ServicoBancario();
            }

            @Override protected void done() {
                carregamento.dispose();
                try {
                    ServicoBancario servico = get();
                    new JanelaPrincipal(new ControladorBancario(servico)).iniciar();
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

    private static void falhaInicializacao(Throwable erro) {
        String detalhe = erro == null || erro.getMessage() == null
                ? "Erro sem detalhes disponíveis." : erro.getMessage();
        JOptionPane.showMessageDialog(null,
                "Não foi possível inicializar o banco local. A interface não será aberta.\n\n" + detalhe,
                "Falha na inicialização", JOptionPane.ERROR_MESSAGE);
    }
}
