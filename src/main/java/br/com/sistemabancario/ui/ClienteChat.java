package br.com.sistemabancario.ui;

import java.util.List;

/** Gera respostas para o chat sem depender da interface Swing nem dos serviços bancários. */
@FunctionalInterface
interface ClienteChat {
    String responder(String mensagem) throws Exception;

    default String responder(String mensagem, List<MensagemConversa> contexto) throws Exception {
        return responder(mensagem);
    }
}
