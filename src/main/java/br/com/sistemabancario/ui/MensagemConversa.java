package br.com.sistemabancario.ui;

/** Mensagem de domínio do diálogo, sem dependência de componentes Swing. */
record MensagemConversa(Papel papel, String conteudo) {
    MensagemConversa {
        if (papel == null) throw new IllegalArgumentException("O papel da mensagem é obrigatório.");
        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException("O conteúdo da mensagem é obrigatório.");
        }
    }

    enum Papel { USUARIO, ASSISTENTE }
}
