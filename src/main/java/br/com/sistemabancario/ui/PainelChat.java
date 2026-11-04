package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Chat informativo; o cliente não recebe controlador, serviço ou acesso à persistência. */
final class PainelChat extends JPanel {
    private final ClienteChat cliente;
    private final JTextArea historico = new JTextArea();
    private final JTextField entrada = new JTextField();
    private final JButton enviar = EstiloSwing.botao("Enviar", true);
    private final JLabel estado = new JLabel("Assistente simulado — dúvidas gerais sobre o sistema.");

    PainelChat(ClienteChat cliente) {
        super();
        this.cliente = cliente;
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setOpaque(false);
        JPanel cabecalho = EstiloSwing.cabecalhoTela("Assistente do sistema",
                "Faça perguntas gerais sobre as funções do Sistema Bancário.");
        cabecalho.setAlignmentX(LEFT_ALIGNMENT);
        cabecalho.setMaximumSize(new Dimension(Integer.MAX_VALUE, cabecalho.getPreferredSize().height));
        add(cabecalho);
        add(javax.swing.Box.createVerticalStrut(14));

        JPanel cartao = new JPanel(new BorderLayout(0, 14));
        EstiloSwing.cartao(cartao);
        cartao.setAlignmentX(LEFT_ALIGNMENT);
        historico.setEditable(false);
        historico.setLineWrap(true);
        historico.setWrapStyleWord(true);
        historico.setFont(EstiloSwing.FONTE_CORPO);
        historico.setForeground(EstiloSwing.TEXTO);
        historico.setBackground(EstiloSwing.FUNDO_INFO);
        historico.setText("Assistente: Olá! Posso explicar as funções gerais do sistema. Não acesso contas nem realizo operações.\n");
        JScrollPane rolagem = EstiloSwing.rolagemNatural(new JScrollPane(historico,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        rolagem.setPreferredSize(new Dimension(330, 230));
        rolagem.setMinimumSize(new Dimension(220, 180));
        rolagem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        rolagem.setBorder(BorderFactory.createLineBorder(EstiloSwing.BORDA));
        rolagem.getViewport().setBackground(EstiloSwing.FUNDO_INFO);
        cartao.add(rolagem, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(0, 9));
        rodape.setOpaque(false);
        JPanel linhaEntrada = new JPanel(new BorderLayout(10, 0));
        linhaEntrada.setOpaque(false);
        EstiloSwing.campo(entrada);
        entrada.setPreferredSize(new Dimension(100, 48));
        enviar.setMargin(new Insets(10, 20, 10, 20));
        linhaEntrada.add(entrada, BorderLayout.CENTER);
        linhaEntrada.add(enviar, BorderLayout.EAST);
        estado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        estado.setForeground(EstiloSwing.SECUNDARIO);
        rodape.add(linhaEntrada, BorderLayout.CENTER);
        rodape.add(estado, BorderLayout.SOUTH);
        cartao.add(rodape, BorderLayout.SOUTH);
        cartao.setPreferredSize(new Dimension(350, 370));
        cartao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 390));
        add(cartao);
        add(javax.swing.Box.createVerticalGlue());

        enviar.addActionListener(evento -> enviarMensagem());
        entrada.addActionListener(evento -> enviarMensagem());
    }

    private void enviarMensagem() {
        String mensagem = entrada.getText().trim();
        if (mensagem.isEmpty()) {
            estado.setText("Digite uma mensagem antes de enviar.");
            estado.setForeground(EstiloSwing.AVISO);
            entrada.requestFocusInWindow();
            return;
        }

        entrada.setText("");
        adicionarMensagem("Você", mensagem);
        estado.setText("Assistente está respondendo…");
        estado.setForeground(EstiloSwing.SECUNDARIO);
        entrada.setEnabled(false);
        enviar.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return cliente.responder(mensagem);
            }

            @Override protected void done() {
                try {
                    String resposta = get();
                    if (resposta == null || resposta.isBlank()) {
                        throw new IllegalStateException("O assistente não retornou uma resposta.");
                    }
                    adicionarMensagem("Assistente", resposta);
                    estado.setText("Resposta simulada pronta.");
                    estado.setForeground(EstiloSwing.SUCESSO);
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    mostrarErro("A resposta foi interrompida.");
                } catch (ExecutionException | RuntimeException erro) {
                    Throwable causa = erro instanceof ExecutionException && erro.getCause() != null
                            ? erro.getCause() : erro;
                    String detalhe = causa.getMessage();
                    mostrarErro(detalhe == null || detalhe.isBlank()
                            ? "Não foi possível obter uma resposta." : detalhe);
                } finally {
                    entrada.setEnabled(true);
                    enviar.setEnabled(true);
                    entrada.requestFocusInWindow();
                }
            }
        }.execute();
    }

    private void adicionarMensagem(String autor, String mensagem) {
        historico.append("\n" + autor + ": " + mensagem + "\n");
        historico.setCaretPosition(historico.getDocument().getLength());
    }

    private void mostrarErro(String mensagem) {
        estado.setText("Erro no chat: " + mensagem);
        estado.setForeground(EstiloSwing.ERRO);
    }
}
