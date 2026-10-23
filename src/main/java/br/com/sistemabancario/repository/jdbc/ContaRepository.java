package br.com.sistemabancario.repository.jdbc;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.exception.PersistenciaException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ContaRepository {
    private final ConexaoBanco banco;

    public ContaRepository(ConexaoBanco banco) { this.banco = banco; }

    public void salvar(Conta conta) {
        banco.emTransacao(conexao -> { salvar(conexao, conta); return null; });
    }

    void salvar(Connection conexao, Conta conta) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO conta (id, cliente_id, saldo) VALUES (?, ?, ?)")) {
            comando.setString(1, conta.getIdentificador().toString());
            comando.setString(2, conta.getTitular().getIdentificador().toString());
            comando.setString(3, conta.getSaldo().toString());
            comando.executeUpdate();
        }
    }

    void atualizarSaldo(Connection conexao, UUID id, BigDecimal anterior, BigDecimal novo) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "UPDATE conta SET saldo = ? WHERE id = ? AND saldo = ?")) {
            comando.setString(1, novo.toString());
            comando.setString(2, id.toString());
            comando.setString(3, anterior.toString());
            if (comando.executeUpdate() != 1) {
                throw new SQLException("Saldo persistido divergente ou conta inexistente: " + id);
            }
        }
    }

    public Optional<Conta> buscar(UUID id) {
        try (Connection conexao = banco.abrir()) { return buscar(conexao, id); }
        catch (SQLException e) { throw new PersistenciaException("Não foi possível consultar a conta.", e); }
    }

    Optional<Conta> buscar(Connection conexao, UUID id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "SELECT c.id, c.saldo, p.id AS cliente_id, p.nome AS cliente_nome " +
                "FROM conta c JOIN cliente p ON p.id = c.cliente_id WHERE c.id = ?")) {
            comando.setString(1, id.toString());
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        }
    }

    public List<Conta> listar() {
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(
                "SELECT c.id, c.saldo, p.id AS cliente_id, p.nome AS cliente_nome " +
                "FROM conta c JOIN cliente p ON p.id = c.cliente_id ORDER BY c.id");
             ResultSet resultado = comando.executeQuery()) {
            List<Conta> contas = new ArrayList<>();
            while (resultado.next()) contas.add(mapear(resultado));
            return List.copyOf(contas);
        } catch (SQLException e) { throw new PersistenciaException("Não foi possível listar as contas.", e); }
    }

    private static Conta mapear(ResultSet resultado) throws SQLException {
        Cliente cliente = new Cliente(UUID.fromString(resultado.getString("cliente_id")), resultado.getString("cliente_nome"));
        return new Conta(UUID.fromString(resultado.getString("id")), cliente,
                new BigDecimal(resultado.getString("saldo")));
    }
}
