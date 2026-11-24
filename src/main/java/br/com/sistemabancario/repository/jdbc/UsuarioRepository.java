package br.com.sistemabancario.repository.jdbc;

import br.com.sistemabancario.exception.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/** Persistência de credenciais com hash e salt; nunca grava senhas em texto puro. */
public final class UsuarioRepository {
    private final ConexaoBanco banco;

    public UsuarioRepository(ConexaoBanco banco) { this.banco = banco; }

    void salvar(Connection conexao, UsuarioPersistido usuario) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO usuario_acesso (id, nome, login, login_normalizado, senha_hash, senha_salt, perfil, cliente_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            comando.setString(1, usuario.id().toString());
            comando.setString(2, usuario.nome());
            comando.setString(3, usuario.login());
            comando.setString(4, usuario.loginNormalizado());
            comando.setString(5, usuario.hashSenha());
            comando.setString(6, usuario.saltSenha());
            comando.setString(7, usuario.perfil());
            comando.setString(8, usuario.clienteId() == null ? null : usuario.clienteId().toString());
            comando.executeUpdate();
        }
    }

    public Optional<UsuarioPersistido> buscarPorLogin(String loginNormalizado) {
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(
                "SELECT id, nome, login, login_normalizado, senha_hash, senha_salt, perfil, cliente_id "
                        + "FROM usuario_acesso WHERE login_normalizado = ?")) {
            comando.setString(1, loginNormalizado);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException erro) {
            throw new PersistenciaException("Não foi possível consultar o usuário.", erro);
        }
    }

    public boolean existeAdministrador() {
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(
                "SELECT 1 FROM usuario_acesso WHERE perfil = 'ADMINISTRADOR' LIMIT 1");
             ResultSet resultado = comando.executeQuery()) {
            return resultado.next();
        } catch (SQLException erro) {
            throw new PersistenciaException("Não foi possível consultar os administradores.", erro);
        }
    }

    boolean existeAdministrador(Connection conexao) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "SELECT 1 FROM usuario_acesso WHERE perfil = 'ADMINISTRADOR' LIMIT 1");
             ResultSet resultado = comando.executeQuery()) {
            return resultado.next();
        }
    }

    private static UsuarioPersistido mapear(ResultSet resultado) throws SQLException {
        String clienteId = resultado.getString("cliente_id");
        return new UsuarioPersistido(UUID.fromString(resultado.getString("id")), resultado.getString("nome"),
                resultado.getString("login"), resultado.getString("login_normalizado"),
                resultado.getString("senha_hash"), resultado.getString("senha_salt"),
                resultado.getString("perfil"), clienteId == null ? null : UUID.fromString(clienteId));
    }
}
