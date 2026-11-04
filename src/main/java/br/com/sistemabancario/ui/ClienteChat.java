package br.com.sistemabancario.ui;

/** Gera respostas para o chat sem depender da interface Swing nem dos serviços bancários. */
@FunctionalInterface
interface ClienteChat {
    String responder(String mensagem) throws Exception;
}
