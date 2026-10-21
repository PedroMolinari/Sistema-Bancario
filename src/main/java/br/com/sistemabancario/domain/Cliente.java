package br.com.sistemabancario.domain;

import java.util.Objects;
import java.util.UUID;

public final class Cliente {
    private final UUID identificador;
    private final String nome;

    public Cliente(String nome) {
        this(UUID.randomUUID(), nome);
    }

    public Cliente(UUID identificador, String nome) {
        this.identificador = Objects.requireNonNull(identificador, "O identificador do cliente é obrigatório.");
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public UUID getIdentificador() { return identificador; }
    public String getNome() { return nome; }
}
