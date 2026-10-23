package br.com.sistemabancario.repository.jdbc;

import br.com.sistemabancario.exception.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Abre conexões SQLite e fornece transações JDBC com commit/rollback explícitos. */
public final class ConexaoBanco {
    @FunctionalInterface
    public interface Operacao<T> {
        T executar(Connection conexao) throws SQLException;
    }

    private final Path arquivo;

    public ConexaoBanco() {
        this(Path.of(System.getProperty("banco.db.path", "data/banco.db")));
    }

    public ConexaoBanco(Path arquivo) {
        this.arquivo = Objects.requireNonNull(arquivo).toAbsolutePath().normalize();
    }

    public Path getArquivo() { return arquivo; }

    public Connection abrir() throws SQLException {
        try {
            Path diretorio = arquivo.getParent();
            if (diretorio != null) Files.createDirectories(diretorio);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível criar o diretório do banco.", e);
        }
        Connection conexao = DriverManager.getConnection("jdbc:sqlite:" + arquivo);
        try (Statement comando = conexao.createStatement()) {
            comando.execute("PRAGMA foreign_keys = ON");
            comando.execute("PRAGMA busy_timeout = 15000");
        } catch (SQLException e) {
            conexao.close();
            throw e;
        }
        return conexao;
    }

    public void inicializar() {
        String script;
        try (InputStream entrada = ConexaoBanco.class.getResourceAsStream("/db/schema.sql")) {
            if (entrada == null) throw new IOException("Recurso /db/schema.sql não encontrado.");
            script = new String(entrada.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o script de criação do banco.", e);
        }
        try (Connection conexao = abrir(); Statement comando = conexao.createStatement()) {
            for (String sql : script.split(";")) {
                if (!sql.isBlank()) comando.execute(sql);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível inicializar o banco SQLite.", e);
        }
    }

    public <T> T emTransacao(Operacao<T> operacao) {
        try (Connection conexao = abrir()) {
            conexao.setAutoCommit(false);
            try {
                T resultado = operacao.executar(conexao);
                conexao.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException falhaRollback) {
                    e.addSuppressed(falhaRollback);
                }
                if (e instanceof SQLException sql) {
                    throw new PersistenciaException("Falha na transação JDBC; alterações revertidas.", sql);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao acessar o banco SQLite.", e);
        }
    }
}
