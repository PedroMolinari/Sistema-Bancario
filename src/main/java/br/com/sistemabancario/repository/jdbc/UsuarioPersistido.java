package br.com.sistemabancario.repository.jdbc;

import java.util.UUID;

/** Dados de acesso persistidos; nunca contém a senha em texto puro. */
public record UsuarioPersistido(UUID id, String nome, String login, String loginNormalizado,
                                String hashSenha, String saltSenha, String perfil, UUID clienteId) { }
