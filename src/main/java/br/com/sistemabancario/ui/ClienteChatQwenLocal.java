package br.com.sistemabancario.ui;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Cliente de conversação geral para um endpoint local compatível com OpenAI Chat Completions. */
final class ClienteChatQwenLocal implements ClienteChat {
    static final URI URL_PADRAO = URI.create("http://127.0.0.1:8081/v1/chat/completions");
    static final String MODELO_PADRAO = "Qwen2.5-1.5B-Instruct";
    static final Duration TIMEOUT_CONEXAO_PADRAO = Duration.ofSeconds(3);
    static final Duration TIMEOUT_RESPOSTA_PADRAO = Duration.ofSeconds(45);
    private static final int TAMANHO_MAXIMO_MENSAGEM = 8_000;
    private static final int MAXIMO_MENSAGENS_CONTEXTO = 8;
    private static final int MAXIMO_CARACTERES_CONTEXTO = 4_000;
    private static final String INSTRUCAO_SISTEMA =
        "Você é o assistente virtual do Sistema Bancário. "
        + "Responda dúvidas gerais sobre o funcionamento do sistema e conceitos bancários básicos. "
        + "Seja objetivo, claro e didático. "
        + "Responda preferencialmente em 2 ou 3 frases, com no máximo 50 palavras. "
        + "Evite listas longas, repetições e explicações desnecessárias. "
        + "Se não souber uma informação, diga isso com clareza. "
        + "Não invente funcionalidades do sistema. "
        + "Não afirme ter acesso a contas, saldos ou transações. "
        + "Não execute nem prometa executar operações bancárias. "
        + "Para pedidos sobre dados pessoais ou operações, explique que este chat não tem esse acesso. "
        + "Seja educado e utilize português brasileiro.";

    private final HttpClient clienteHttp;
    private final URI endpoint;
    private final String modelo;
    private final Duration timeoutResposta;

    ClienteChatQwenLocal() {
        this(URI.create(System.getProperty("qwen.chat.url", URL_PADRAO.toString())),
                System.getProperty("qwen.chat.model", MODELO_PADRAO),
                TIMEOUT_CONEXAO_PADRAO, TIMEOUT_RESPOSTA_PADRAO);
    }

    ClienteChatQwenLocal(URI endpoint, String modelo, Duration timeoutConexao, Duration timeoutResposta) {
        this(endpoint, modelo, timeoutResposta, HttpClient.newBuilder()
                .connectTimeout(validarTimeout(timeoutConexao, "conexão"))
                .build());
    }

    ClienteChatQwenLocal(URI endpoint, String modelo, Duration timeoutResposta, HttpClient clienteHttp) {
        this.endpoint = Objects.requireNonNull(endpoint, "O endpoint Qwen é obrigatório.");
        if (!"http".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getHost() == null) {
            throw new IllegalArgumentException("O endpoint Qwen deve ser uma URL HTTP válida.");
        }
        if (!(endpoint.getHost().equals("127.0.0.1") || endpoint.getHost().equalsIgnoreCase("localhost")
                || endpoint.getHost().equals("::1"))) {
            throw new IllegalArgumentException("O endpoint Qwen deve apontar para o servidor local.");
        }
        if (modelo == null || modelo.isBlank()) throw new IllegalArgumentException("O modelo Qwen é obrigatório.");
        this.modelo = modelo.trim();
        this.timeoutResposta = validarTimeout(timeoutResposta, "resposta");
        this.clienteHttp = Objects.requireNonNull(clienteHttp, "O cliente HTTP é obrigatório.");
    }

    @Override
    public String responder(String mensagem) {
        return responder(mensagem, List.of());
    }

    @Override
    public String responder(String mensagem, List<MensagemConversa> contexto) {
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Digite uma mensagem para o assistente.");
        }
        if (mensagem.length() > TAMANHO_MAXIMO_MENSAGEM) {
            throw new IllegalArgumentException("A mensagem excede o limite de 8.000 caracteres.");
        }

        String corpo = corpoRequisicao(mensagem, contexto == null ? List.of() : contexto);
        HttpRequest requisicao = HttpRequest.newBuilder(endpoint)
                .timeout(timeoutResposta)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo, java.nio.charset.StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> resposta = clienteHttp.send(requisicao,
                    HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
                throw new IllegalStateException("O servidor Qwen respondeu com HTTP " + resposta.statusCode() + ".");
            }
            return extrairConteudo(resposta.body());
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A solicitação ao servidor Qwen foi interrompida.", erro);
        } catch (java.net.http.HttpConnectTimeoutException erro) {
            throw new IllegalStateException("Tempo limite ao conectar ao Qwen em " + endpoint
                    + ". Verifique se o llama-server está iniciado e acessível.", erro);
        } catch (java.net.http.HttpTimeoutException erro) {
            throw new IllegalStateException("O Qwen em " + endpoint + " excedeu o tempo limite de resposta.", erro);
        } catch (java.net.ConnectException erro) {
            throw new IllegalStateException("Conexão recusada pelo Qwen em " + endpoint
                    + ". Inicie o llama-server com o modelo Qwen carregado e ouvindo nessa porta.", erro);
        } catch (IOException erro) {
            throw new IllegalStateException("Falha de comunicação com o Qwen em " + endpoint + ": "
                    + erro.getClass().getSimpleName() + (erro.getMessage() == null ? "." : " — " + erro.getMessage()), erro);
        }
    }

    private String corpoRequisicao(String mensagem, List<MensagemConversa> contexto) {
        StringBuilder corpo = new StringBuilder("{\"model\":")
                .append(Json.escrever(modelo))
                .append(",\"messages\":[{\"role\":\"system\",\"content\":")
                .append(Json.escrever(INSTRUCAO_SISTEMA)).append('}');
        for (MensagemConversa anterior : selecionarContextoRecente(contexto)) {
            String papel = anterior.papel() == MensagemConversa.Papel.USUARIO ? "user" : "assistant";
            corpo.append(", {\"role\":").append(Json.escrever(papel))
                    .append(",\"content\":").append(Json.escrever(anterior.conteudo())).append('}');
        }
        return corpo.append(", {\"role\":\"user\",\"content\":")
                .append(Json.escrever(mensagem)).append("}],\"stream\":false}").toString();
    }

    private static List<MensagemConversa> selecionarContextoRecente(List<MensagemConversa> contexto) {
        List<MensagemConversa> recentesReversos = new ArrayList<>();
        int caracteres = 0;
        for (int indice = contexto.size() - 1;
                indice >= 0 && recentesReversos.size() < MAXIMO_MENSAGENS_CONTEXTO; indice--) {
            MensagemConversa mensagem = contexto.get(indice);
            if (mensagem == null) continue;
            int tamanho = mensagem.conteudo().length();
            if (caracteres + tamanho > MAXIMO_CARACTERES_CONTEXTO) break;
            recentesReversos.add(mensagem);
            caracteres += tamanho;
        }
        java.util.Collections.reverse(recentesReversos);
        return recentesReversos;
    }

    private static String extrairConteudo(String json) {
        final Object raiz;
        try {
            raiz = new Json(json).ler();
        } catch (IllegalArgumentException erro) {
            throw new IllegalStateException("O servidor Qwen retornou JSON inválido.", erro);
        }
        if (!(raiz instanceof Map<?, ?> objeto)) throw respostaInvalida();
        Object escolhas = objeto.get("choices");
        if (!(escolhas instanceof List<?> lista) || lista.isEmpty()
                || !(lista.getFirst() instanceof Map<?, ?> escolha)
                || !(escolha.get("message") instanceof Map<?, ?> mensagem)
                || !(mensagem.get("content") instanceof String conteudo)
                || conteudo.isBlank()) {
            throw respostaInvalida();
        }
        return conteudo.trim();
    }

    private static IllegalStateException respostaInvalida() {
        return new IllegalStateException("A resposta do servidor Qwen não contém choices[0].message.content válido.");
    }

    private static Duration validarTimeout(Duration timeout, String tipo) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("O timeout de " + tipo + " deve ser positivo.");
        }
        return timeout;
    }

    /** Parser JSON pequeno e estrito para ler a resposta sem adicionar dependência ao projeto. */
    private static final class Json {
        private final String texto;
        private int indice;

        private Json(String texto) {
            if (texto == null) throw erro("JSON ausente");
            this.texto = texto;
        }

        private Object ler() {
            Object valor = valor();
            espacos();
            if (indice != texto.length()) throw erro("Conteúdo após o valor JSON");
            return valor;
        }

        private Object valor() {
            espacos();
            if (indice >= texto.length()) throw erro("Valor JSON incompleto");
            return switch (texto.charAt(indice)) {
                case '{' -> objeto();
                case '[' -> lista();
                case '"' -> string();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> numero();
            };
        }

        private Map<String, Object> objeto() {
            indice++;
            espacos();
            Map<String, Object> objeto = new LinkedHashMap<>();
            if (consumir('}')) return objeto;
            do {
                espacos();
                if (indice >= texto.length() || texto.charAt(indice) != '"') throw erro("Chave inválida");
                String chave = string();
                espacos();
                exigir(':');
                if (objeto.containsKey(chave)) throw erro("Chave duplicada");
                objeto.put(chave, valor());
                espacos();
                if (consumir('}')) return objeto;
                exigir(',');
            } while (true);
        }

        private List<Object> lista() {
            indice++;
            espacos();
            List<Object> lista = new ArrayList<>();
            if (consumir(']')) return lista;
            do {
                lista.add(valor());
                espacos();
                if (consumir(']')) return lista;
                exigir(',');
            } while (true);
        }

        private String string() {
            exigir('"');
            StringBuilder resultado = new StringBuilder();
            while (indice < texto.length()) {
                char caractere = texto.charAt(indice++);
                if (caractere == '"') return resultado.toString();
                if (caractere < 0x20) throw erro("Caractere de controle em string");
                if (caractere != '\\') {
                    resultado.append(caractere);
                    continue;
                }
                if (indice >= texto.length()) throw erro("Escape incompleto");
                char escape = texto.charAt(indice++);
                switch (escape) {
                    case '"', '\\', '/' -> resultado.append(escape);
                    case 'b' -> resultado.append('\b');
                    case 'f' -> resultado.append('\f');
                    case 'n' -> resultado.append('\n');
                    case 'r' -> resultado.append('\r');
                    case 't' -> resultado.append('\t');
                    case 'u' -> resultado.append(unicode());
                    default -> throw erro("Escape inválido");
                }
            }
            throw erro("String não terminada");
        }

        private char unicode() {
            if (indice + 4 > texto.length()) throw erro("Escape Unicode incompleto");
            try {
                char resultado = (char) Integer.parseInt(texto.substring(indice, indice + 4), 16);
                indice += 4;
                return resultado;
            } catch (NumberFormatException causa) {
                throw erro("Escape Unicode inválido");
            }
        }

        private Object numero() {
            int inicio = indice;
            if (consumir('-')) { /* sinal */ }
            if (consumir('0')) {
                if (indice < texto.length() && texto.charAt(indice) >= '0' && texto.charAt(indice) <= '9') {
                    throw erro("Zero inicial inválido");
                }
            } else {
                if (indice >= texto.length() || texto.charAt(indice) < '1' || texto.charAt(indice) > '9') {
                    throw erro("Número JSON inválido");
                }
                digitos();
            }
            if (consumir('.')) digitos();
            if (consumir('e') || consumir('E')) {
                if (!consumir('+')) consumir('-');
                digitos();
            }
            if (inicio == indice) throw erro("Valor JSON inválido");
            try {
                return new java.math.BigDecimal(texto.substring(inicio, indice));
            } catch (NumberFormatException causa) {
                throw erro("Número JSON inválido");
            }
        }

        private void digitos() {
            int inicio = indice;
            while (indice < texto.length() && texto.charAt(indice) >= '0' && texto.charAt(indice) <= '9') indice++;
            if (inicio == indice) throw erro("Número JSON inválido");
        }

        private Object literal(String literal, Object valor) {
            if (!texto.startsWith(literal, indice)) throw erro("Literal JSON inválido");
            indice += literal.length();
            return valor;
        }

        private boolean consumir(char esperado) {
            if (indice < texto.length() && texto.charAt(indice) == esperado) {
                indice++;
                return true;
            }
            return false;
        }

        private void exigir(char esperado) {
            if (!consumir(esperado)) throw erro("Esperado '" + esperado + "'");
        }

        private void espacos() {
            while (indice < texto.length()) {
                char caractere = texto.charAt(indice);
                if (caractere != ' ' && caractere != '\t' && caractere != '\n' && caractere != '\r') break;
                indice++;
            }
        }

        private IllegalArgumentException erro(String mensagem) {
            return new IllegalArgumentException(mensagem + " no índice " + indice + ".");
        }

        private static String escrever(String valor) {
            StringBuilder resultado = new StringBuilder("\"");
            for (int i = 0; i < valor.length(); i++) {
                char caractere = valor.charAt(i);
                switch (caractere) {
                    case '"' -> resultado.append("\\\"");
                    case '\\' -> resultado.append("\\\\");
                    case '\b' -> resultado.append("\\b");
                    case '\f' -> resultado.append("\\f");
                    case '\n' -> resultado.append("\\n");
                    case '\r' -> resultado.append("\\r");
                    case '\t' -> resultado.append("\\t");
                    default -> {
                        if (caractere < 0x20) resultado.append(String.format("\\u%04x", (int) caractere));
                        else resultado.append(caractere);
                    }
                }
            }
            return resultado.append('"').toString();
        }
    }
}
