package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** Coordena o envio e o estado da conversa, sem expor serviços bancários ao cliente de IA. */
final class PainelChat extends JPanel {
    private final ClienteChat cliente;
    private final PainelHistoricoChat historico = new PainelHistoricoChat();
    private final List<MensagemConversa> conversa = new ArrayList<>();
    private final JTextField entrada = new JTextField();
    private final JButton enviar = EstiloSwing.botaoCompacto("Enviar", true);
    private final JLabel estado = new JLabel(" ");
    private final JScrollPane rolagem;
    private final Timer temporizadorRolagem;
    private SwingWorker<String, Void> respostaAtual;

    PainelChat(ClienteChat cliente) {
        super(new BorderLayout());
        this.cliente = cliente;
        setOpaque(false);
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        historico.adicionarMensagem("Assistente", "Olá! Posso explicar as funções gerais do sistema."
                + " Não acesso contas nem realizo operações.", false);
        rolagem = EstiloSwing.rolagemNatural(new JScrollPane(historico,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        temporizadorRolagem = new Timer(60, evento -> {
            var barraVertical = rolagem.getVerticalScrollBar();
            barraVertical.setValue(barraVertical.getMaximum());
        });
        temporizadorRolagem.setRepeats(false);
        add(rolagem, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(0, 7));
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createEmptyBorder(8, 20, 16, 20));
        JPanel linhaEntrada = new JPanel(new BorderLayout(10, 0));
        linhaEntrada.setOpaque(false);
        EstiloSwing.campo(entrada);
        entrada.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        entrada.setToolTipText("Digite uma dúvida geral sobre o sistema");
        entrada.getAccessibleContext().setAccessibleName("Mensagem para o assistente");
        entrada.setPreferredSize(new Dimension(100, EstiloSwing.TAMANHO_CAMPO.height));
        enviar.setToolTipText("Enviar mensagem");
        enviar.getAccessibleContext().setAccessibleName("Enviar mensagem ao assistente");
        enviar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        linhaEntrada.add(entrada, BorderLayout.CENTER);
        linhaEntrada.add(enviar, BorderLayout.EAST);

        estado.setFont(EstiloSwing.FONTE_AUXILIAR);
        estado.setForeground(EstiloSwing.SECUNDARIO);
        rodape.add(linhaEntrada, BorderLayout.CENTER);
        rodape.add(estado, BorderLayout.SOUTH);
        add(rodape, BorderLayout.SOUTH);

        enviar.addActionListener(evento -> {
            if (respostaAtual != null && !respostaAtual.isDone()) cancelarResposta();
            else enviarMensagem();
        });
        entrada.addActionListener(evento -> enviarMensagem());
    }

    private void enviarMensagem() {
        if (respostaAtual != null && !respostaAtual.isDone()) return;
        String mensagem = entrada.getText().trim();
        if (mensagem.isEmpty()) {
            definirEstado("Digite uma mensagem antes de enviar.", EstiloSwing.AVISO);
            entrada.requestFocusInWindow();
            return;
        }

        entrada.setText("");
        List<MensagemConversa> contexto = List.copyOf(conversa);
        conversa.add(new MensagemConversa(MensagemConversa.Papel.USUARIO, mensagem));
        adicionarMensagem("Você", mensagem, true);
        definirEstado("Qwen está respondendo…", EstiloSwing.SECUNDARIO);
        entrada.setEnabled(false);
        configurarBotaoCancelar();

        respostaAtual = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return cliente.responder(mensagem, contexto);
            }

            @Override protected void done() {
                try {
                    String resposta = get();
                    if (resposta == null || resposta.isBlank()) {
                        throw new IllegalStateException("O assistente não retornou uma resposta.");
                    }
                    conversa.add(new MensagemConversa(MensagemConversa.Papel.ASSISTENTE, resposta));
                    adicionarMensagem("Assistente", resposta, false);
                    definirEstado("Resposta pronta.", EstiloSwing.SUCESSO);
                } catch (CancellationException erro) {
                    definirEstado("Geração cancelada.", EstiloSwing.SECUNDARIO);
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
                    respostaAtual = null;
                    entrada.setEnabled(true);
                    configurarBotaoEnviar();
                    entrada.requestFocusInWindow();
                }
            }
        };
        respostaAtual.execute();
    }

    private void cancelarResposta() {
        definirEstado("Cancelando geração…", EstiloSwing.SECUNDARIO);
        enviar.setEnabled(false);
        respostaAtual.cancel(true);
    }

    private void adicionarMensagem(String autor, String texto, boolean usuario) {
        historico.adicionarMensagem(autor, texto, usuario);
        historico.revalidate();
        rolagem.revalidate();
        rolagem.getViewport().revalidate();
        SwingUtilities.invokeLater(() -> {
            rolagem.revalidate();
            rolagem.getViewport().revalidate();
            temporizadorRolagem.restart();
        });
    }

    private void configurarBotaoCancelar() {
        enviar.setText("Cancelar");
        enviar.setToolTipText("Interromper a geração atual");
        enviar.getAccessibleContext().setAccessibleName("Cancelar resposta do assistente");
        enviar.setEnabled(true);
    }

    private void configurarBotaoEnviar() {
        enviar.setText("Enviar");
        enviar.setToolTipText("Enviar mensagem");
        enviar.getAccessibleContext().setAccessibleName("Enviar mensagem ao assistente");
        enviar.setEnabled(true);
    }

    private void mostrarErro(String mensagem) {
        definirEstado("Erro no chat: " + mensagem, EstiloSwing.ERRO);
        estado.setToolTipText(mensagem);
    }

    private void definirEstado(String mensagem, java.awt.Color cor) {
        estado.setText(mensagem);
        estado.setForeground(cor);
        estado.setToolTipText(null);
    }
}
