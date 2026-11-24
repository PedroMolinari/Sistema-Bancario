#!/bin/bash

set -u

PROJECT_DIR="/Users/carlosmolinari/Desktop/Sistema Bancario"
LLAMA_SERVER="/Users/carlosmolinari/modelos-ia/llama.cpp/build/bin/llama-server"
MODEL="/Users/carlosmolinari/modelos-ia/modelos/qwen2.5-1.5b-instruct-q4_k_m.gguf"
HOST="127.0.0.1"
PORT="8081"
CONTEXT="2048"
THREADS="2"
MODEL_ALIAS="Qwen2.5-1.5B-Instruct"
HEALTH_URL="http://${HOST}:${PORT}/health"
CHAT_URL="http://${HOST}:${PORT}/v1/chat/completions"
STARTUP_TIMEOUT="120"

# Finder starts commands with a minimal PATH; include common macOS/Homebrew locations.
PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${PATH:-}"
export PATH

SERVER_PID=""
SERVER_LOG=""
STARTED_SERVER=0

mensagem() { printf '\n%s\n' "$*"; }
erro() {
    mensagem "ERRO: $*"
    mensagem "Pressione Enter para fechar esta janela."
    read -r _
    exit 1
}

limpar_servidor_iniciado() {
    [ "$STARTED_SERVER" -eq 1 ] || return 0
    [ -n "$SERVER_PID" ] || return 0

    # Guard against signalling a recycled PID: verify it is still our llama-server executable.
    comando_atual="$(ps -p "$SERVER_PID" -o command= 2>/dev/null || true)"
    case "$comando_atual" in
        "$LLAMA_SERVER"*)
            mensagem "Encerrando o llama-server iniciado por este inicializador..."
            kill "$SERVER_PID" 2>/dev/null || true
            contador=0
            while kill -0 "$SERVER_PID" 2>/dev/null && [ "$contador" -lt 5 ]; do
                sleep 1
                contador=$((contador + 1))
            done
            if kill -0 "$SERVER_PID" 2>/dev/null; then
                comando_atual="$(ps -p "$SERVER_PID" -o command= 2>/dev/null || true)"
                case "$comando_atual" in
                    "$LLAMA_SERVER"*) kill -TERM "$SERVER_PID" 2>/dev/null || true ;;
                esac
            fi
            ;;
    esac
}

ao_sair() {
    codigo=$?
    trap - EXIT INT TERM HUP
    limpar_servidor_iniciado
    exit "$codigo"
}
trap ao_sair EXIT
trap 'exit 130' INT
trap 'exit 143' TERM HUP

cd "$PROJECT_DIR" 2>/dev/null || erro "Pasta do projeto não encontrada: $PROJECT_DIR"

[ -x "$LLAMA_SERVER" ] || erro "llama-server não encontrado ou sem permissão de execução: $LLAMA_SERVER"
[ -f "$MODEL" ] || erro "Arquivo do modelo não encontrado: $MODEL"
command -v mvn >/dev/null 2>&1 || erro "Maven não encontrado no PATH. Instale/configure Maven 3.9+ e tente novamente."
command -v curl >/dev/null 2>&1 || erro "curl não encontrado; é necessário para verificar o endpoint /health."
command -v nc >/dev/null 2>&1 || erro "nc não encontrado; é necessário para detectar uma porta 8081 ocupada."

mensagem "Sistema Bancário — inicialização"
mensagem "Projeto: $PROJECT_DIR"
mensagem "Modelo: $MODEL"

if curl --silent --show-error --fail --max-time 2 "$HEALTH_URL" >/dev/null 2>&1; then
    mensagem "Qwen já está saudável em $HEALTH_URL; reutilizando o servidor existente."
elif nc -z -w 1 "$HOST" "$PORT" >/dev/null 2>&1; then
    erro "A porta $PORT está ocupada, mas $HEALTH_URL não respondeu com sucesso. Nenhum processo foi iniciado."
else
    SERVER_LOG="$(mktemp -t sistema-bancario-qwen.XXXXXX 2>/dev/null)" \
        || erro "Não foi possível criar um arquivo temporário de log."
    mensagem "Iniciando llama-server em segundo plano..."
    "$LLAMA_SERVER" -m "$MODEL" --host "$HOST" --port "$PORT" \
        -c "$CONTEXT" -t "$THREADS" --alias "$MODEL_ALIAS" >"$SERVER_LOG" 2>&1 &
    SERVER_PID=$!
    STARTED_SERVER=1

    tentativa=0
    while [ "$tentativa" -lt "$STARTUP_TIMEOUT" ]; do
        if curl --silent --show-error --fail --max-time 1 "$HEALTH_URL" >/dev/null 2>&1; then
            break
        fi
        if ! kill -0 "$SERVER_PID" 2>/dev/null; then
            break
        fi
        sleep 1
        tentativa=$((tentativa + 1))
    done

    if ! curl --silent --show-error --fail --max-time 2 "$HEALTH_URL" >/dev/null 2>&1; then
        detalhe="O servidor não ficou saudável em ${STARTUP_TIMEOUT}s."
        if [ -n "$SERVER_LOG" ] && [ -f "$SERVER_LOG" ]; then
            detalhe="${detalhe}\nÚltimas linhas do log:"
            detalhe="${detalhe}\n$(tail -n 12 "$SERVER_LOG")"
            detalhe="${detalhe}\nLog completo: $SERVER_LOG"
        fi
        erro "$detalhe"
    fi
    mensagem "Qwen pronto em $HEALTH_URL."
fi

mensagem "Iniciando a interface Swing. Feche a aplicação para encerrar este inicializador."
mvn exec:java
codigo_maven=$?
if [ "$codigo_maven" -ne 0 ]; then
    erro "A aplicação Maven terminou com código $codigo_maven."
fi
