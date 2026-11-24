package br.com.sistemabancario.ui;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteChatQwenLocalTest {
    private HttpServer servidor;

    @AfterEach
    void pararServidor() {
        if (servidor != null) servidor.stop(0);
    }

    @Test
    void enviaChatCompletionsSemChaveEExtraiConteudoDaResposta() throws Exception {
        AtomicReference<String> corpoRecebido = new AtomicReference<>();
        AtomicReference<String> autorizacao = new AtomicReference<>();
        AtomicReference<String> metodo = new AtomicReference<>();
        iniciarServidor(exchange -> {
            metodo.set(exchange.getRequestMethod());
            corpoRecebido.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            autorizacao.set(exchange.getRequestHeaders().getFirst("Authorization"));
            responder(exchange, 200, """
                    {"choices":[{"index":0,"message":{"role":"assistant","content":"Olá, \\\"mundo\\\"!\\nTudo bem?"},"finish_reason":"stop"}]}
                    """);
        });

        String resposta = cliente(Duration.ofSeconds(2)).responder("O que é o Pix? (instantâneo)! Diga \"olá\"\\por favor");

        assertEquals("Olá, \"mundo\"!\nTudo bem?", resposta);
        assertEquals("POST", metodo.get());
        assertNull(autorizacao.get(), "A API local não deve receber chave ou cabeçalho de autorização.");
        assertTrue(corpoRecebido.get().contains("\"model\":\"Qwen-TESTE\""));
        assertTrue(corpoRecebido.get().contains("\"role\":\"system\""));
        assertTrue(corpoRecebido.get().contains("O que é o Pix? (instantâneo)! Diga "));
        assertTrue(corpoRecebido.get().contains("\\\"olá\\\"\\\\por favor"));
        assertTrue(corpoRecebido.get().contains("\"stream\":false"));
    }

    @Test
    void incluiHistoricoRecenteEmOrdemEExcluiMensagensAntigas() throws Exception {
        AtomicReference<String> corpoRecebido = new AtomicReference<>();
        iniciarServidor(exchange -> {
            corpoRecebido.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            responder(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"certo\"}}]}");
        });
        List<MensagemConversa> contexto = new ArrayList<>(List.of(
                new MensagemConversa(MensagemConversa.Papel.USUARIO, "mensagem antiga 0"),
                new MensagemConversa(MensagemConversa.Papel.ASSISTENTE, "resposta antiga 0"),
                new MensagemConversa(MensagemConversa.Papel.USUARIO, "mensagem antiga 1"),
                new MensagemConversa(MensagemConversa.Papel.ASSISTENTE, "resposta antiga 1"),
                new MensagemConversa(MensagemConversa.Papel.USUARIO, "mensagem antiga 2"),
                new MensagemConversa(MensagemConversa.Papel.ASSISTENTE, "resposta antiga 2"),
                new MensagemConversa(MensagemConversa.Papel.USUARIO, "mensagem antiga 3"),
                new MensagemConversa(MensagemConversa.Papel.ASSISTENTE, "resposta antiga 3"),
                new MensagemConversa(MensagemConversa.Papel.USUARIO, "mensagem antiga 4"),
                new MensagemConversa(MensagemConversa.Papel.ASSISTENTE, "resposta antiga 4")
        ));
        contexto.addAll(List.of(
                new MensagemConversa(MensagemConversa.Papel.USUARIO, "pergunta recente?"),
                new MensagemConversa(MensagemConversa.Papel.ASSISTENTE, "resposta recente!")
        ));

        assertEquals("certo", cliente(Duration.ofSeconds(2)).responder("nova pergunta", contexto));

        String corpo = corpoRecebido.get();
        assertFalse(corpo.contains("mensagem antiga 0"));
        assertTrue(corpo.indexOf("pergunta recente?") < corpo.indexOf("resposta recente!"));
        assertTrue(corpo.indexOf("resposta recente!") < corpo.indexOf("nova pergunta"));
        assertTrue(corpo.contains("\"role\":\"assistant\""));
    }

    @Test
    void rejeitaRespostaHttpDeErro() throws Exception {
        iniciarServidor(exchange -> responder(exchange, 503, "servidor indisponível"));

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> cliente(Duration.ofSeconds(2)).responder("Olá"));

        assertTrue(erro.getMessage().contains("HTTP 503"));
    }

    @Test
    void informaJsonInvalido() throws Exception {
        iniciarServidor(exchange -> responder(exchange, 200,
                "{\"choices\":[],\"duplicada\":null,\"duplicada\":\"valor\"}"));

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> cliente(Duration.ofSeconds(2)).responder("Olá"));

        assertTrue(erro.getMessage().contains("JSON inválido"));
    }

    @Test
    void rejeitaNumeroJsonComZeroInicial() throws Exception {
        iniciarServidor(exchange -> responder(exchange, 200,
                "{\"uso\":01,\"choices\":[{\"message\":{\"content\":\"ok\"}}]}"));

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> cliente(Duration.ofSeconds(2)).responder("Olá"));

        assertTrue(erro.getMessage().contains("JSON inválido"));
    }

    @Test
    void informaRespostaSemConteudoDeChatCompletions() throws Exception {
        iniciarServidor(exchange -> responder(exchange, 200, "{\"choices\":[]}"));

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> cliente(Duration.ofSeconds(2)).responder("Olá"));

        assertTrue(erro.getMessage().contains("choices[0].message.content"));
    }

    @Test
    void informaServidorLocalIndisponivel() throws Exception {
        int porta;
        try (ServerSocket socket = new ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress())) {
            porta = socket.getLocalPort();
        }
        ClienteChatQwenLocal cliente = new ClienteChatQwenLocal(
                URI.create("http://127.0.0.1:" + porta + "/v1/chat/completions"), "Qwen-TESTE",
                Duration.ofSeconds(1), Duration.ofSeconds(1));

        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> cliente.responder("Olá"));

        assertTrue(erro.getMessage().contains("Conexão recusada pelo Qwen"));
        assertTrue(erro.getMessage().contains("127.0.0.1:" + porta));
    }

    @Test
    void respeitaTimeoutDeResposta() throws Exception {
        iniciarServidor(exchange -> {
            try {
                Thread.sleep(400);
                responder(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"tarde\"}}]}");
            } catch (InterruptedException erro) {
                Thread.currentThread().interrupt();
            } catch (IOException ignorada) {
                // O cliente pode fechar a conexão ao atingir seu timeout.
            }
        });

        ClienteChatQwenLocal cliente = new ClienteChatQwenLocal(endpoint(), "Qwen-TESTE",
                Duration.ofMillis(100), java.net.http.HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(1)).build());
        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> cliente.responder("Olá"));

        assertTrue(erro.getMessage().contains("tempo limite"));
    }

    @Test
    void aceitaSomenteEndpointLoopback() {
        assertThrows(IllegalArgumentException.class, () -> new ClienteChatQwenLocal(
                URI.create("http://example.com/v1/chat/completions"), "Qwen-TESTE",
                Duration.ofSeconds(1), Duration.ofSeconds(1)));
    }

    @Test
    void rejeitaMensagemAcimaDoLimiteAntesDeEnviarRequisicao() {
        ClienteChatQwenLocal cliente = new ClienteChatQwenLocal(
                URI.create("http://127.0.0.1:8081/v1/chat/completions"), "Qwen-TESTE",
                Duration.ofSeconds(1), Duration.ofSeconds(1));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> cliente.responder("a".repeat(8_001)));

        assertTrue(erro.getMessage().contains("8.000 caracteres"));
    }

    private ClienteChatQwenLocal cliente(Duration timeoutResposta) {
        return new ClienteChatQwenLocal(endpoint(), "Qwen-TESTE", Duration.ofSeconds(1), timeoutResposta);
    }

    private URI endpoint() {
        return URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1/chat/completions");
    }

    private void iniciarServidor(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/v1/chat/completions", handler);
        servidor.start();
    }

    private static void responder(com.sun.net.httpserver.HttpExchange exchange, int status, String corpo)
            throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var saida = exchange.getResponseBody()) {
            saida.write(bytes);
        }
    }
}
