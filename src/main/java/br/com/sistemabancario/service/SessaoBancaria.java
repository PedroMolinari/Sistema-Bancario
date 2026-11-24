package br.com.sistemabancario.service;

import java.util.UUID;

/** Identificador de sessão emitido somente após autenticação ou cadastro bem-sucedido. */
public final class SessaoBancaria {
    private final UUID identificador;

    SessaoBancaria(UUID identificador) { this.identificador = identificador; }

    UUID identificadorInterno() { return identificador; }
}
