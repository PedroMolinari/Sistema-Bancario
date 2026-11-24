# Inicializador do Sistema Bancário (macOS)

## Requisitos

- macOS Monterey, JDK 21 e Maven 3.9+ disponíveis no `PATH`.
- `llama-server` em `/Users/carlosmolinari/modelos-ia/llama.cpp/build/bin/llama-server`.
- Modelo `qwen2.5-1.5b-instruct-q4_k_m.gguf` no caminho configurado no inicializador.
- `curl` e `nc` (utilitários do macOS).

## Uso

1. Dê dois cliques em **Iniciar Sistema Bancario.command** na raiz do projeto.
2. Aguarde a confirmação de que o endpoint local `/health` está pronto; então a interface será iniciada.
3. Feche a aplicação para encerrar o inicializador. Ele encerra somente o `llama-server` que iniciou. Um servidor saudável que já estava ativo é reutilizado e permanece ligado.

O inicializador usa `127.0.0.1:8080`, contexto 2048 e duas threads. Se o macOS bloquear a primeira abertura, use o menu de contexto **Abrir** e confirme a execução.
