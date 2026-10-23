package br.com.sistemabancario.repository.jdbc;

import br.com.sistemabancario.domain.Transacao;
import br.com.sistemabancario.exception.PersistenciaException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TransacaoRepository {
    private final ConexaoBanco banco;

    public TransacaoRepository(ConexaoBanco banco) { this.banco = banco; }

    void salvar(Connection conexao, Transacao transacao) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "INSERT INTO transacao (id, tipo, valor, data_hora, conta_origem, conta_destino, resultado) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'CONCLUIDA')")) {
            comando.setString(1, transacao.identificador().toString());
            comando.setString(2, transacao.tipo().name());
            comando.setString(3, transacao.valor().toString());
            comando.setString(4, transacao.dataHora().toString());
            comando.setString(5, transacao.contaOrigem().toString());
            comando.setString(6, transacao.contaDestino() == null ? null : transacao.contaDestino().toString());
            comando.executeUpdate();
        }
    }

    public List<Transacao> listarPorConta(UUID contaId) {
        String sql = "SELECT id, tipo, valor, data_hora, conta_origem, conta_destino FROM transacao " +
                "WHERE conta_origem = ? OR conta_destino = ? ORDER BY data_hora, id";
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, contaId.toString());
            comando.setString(2, contaId.toString());
            try (ResultSet resultado = comando.executeQuery()) {
                List<Transacao> transacoes = new ArrayList<>();
                while (resultado.next()) {
                    String destino = resultado.getString("conta_destino");
                    transacoes.add(new Transacao(UUID.fromString(resultado.getString("id")),
                            Transacao.Tipo.valueOf(resultado.getString("tipo")),
                            new BigDecimal(resultado.getString("valor")),
                            LocalDateTime.parse(resultado.getString("data_hora")),
                            UUID.fromString(resultado.getString("conta_origem")),
                            destino == null ? null : UUID.fromString(destino)));
                }
                return List.copyOf(transacoes);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível consultar o extrato.", e);
        }
    }
}
