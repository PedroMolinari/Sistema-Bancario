package br.com.sistemabancario.repository.jdbc;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.exception.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class ClienteRepository {
    private final ConexaoBanco banco;

    public ClienteRepository(ConexaoBanco banco) { this.banco = banco; }

    public void salvar(Cliente cliente) {
        banco.emTransacao(conexao -> { salvar(conexao, cliente); return null; });
    }

    void salvar(Connection conexao, Cliente cliente) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO cliente (id, nome) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET nome = excluded.nome")) {
            comando.setString(1, cliente.getIdentificador().toString());
            comando.setString(2, cliente.getNome());
            comando.executeUpdate();
        }
    }

    public Optional<Cliente> buscar(UUID id) {
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(
                "SELECT id, nome FROM cliente WHERE id = ?")) {
            comando.setString(1, id.toString());
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next()
                        ? Optional.of(new Cliente(UUID.fromString(resultado.getString("id")), resultado.getString("nome")))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível consultar o cliente.", e);
        }
    }
}
