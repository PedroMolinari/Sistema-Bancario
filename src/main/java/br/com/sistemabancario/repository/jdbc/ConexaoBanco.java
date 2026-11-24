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
            boolean possuiNome = false;
            try (var colunas = comando.executeQuery("PRAGMA table_info(usuario_acesso)")) {
                while (colunas.next()) if ("nome".equalsIgnoreCase(colunas.getString("name"))) possuiNome = true;
            }
            if (!possuiNome) comando.execute("ALTER TABLE usuario_acesso ADD COLUMN nome TEXT NOT NULL DEFAULT ''");
            migrarNumeroPublicoConta(conexao, comando);
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível inicializar o banco SQLite.", e);
        }
    }

    /** Adiciona números públicos sem alterar os UUIDs internos nem remover contas já existentes. */
    private static void migrarNumeroPublicoConta(Connection conexao, Statement comando) throws SQLException {
        boolean possuiNumero = false;
        try (var colunas = comando.executeQuery("PRAGMA table_info(conta)")) {
            while (colunas.next()) {
                if ("numero_publico".equalsIgnoreCase(colunas.getString("name"))) possuiNumero = true;
            }
        }
        if (!possuiNumero) comando.execute("ALTER TABLE conta ADD COLUMN numero_publico INTEGER");

        long proximoNumero = 1;
        try (var resultado = comando.executeQuery("SELECT COALESCE(MAX(numero_publico), 0) FROM conta")) {
            if (resultado.next()) proximoNumero = resultado.getLong(1) + 1;
        }
        java.util.List<String> ids = new java.util.ArrayList<>();
        try (var pendentes = comando.executeQuery("SELECT id FROM conta WHERE numero_publico IS NULL ORDER BY id")) {
            while (pendentes.next()) ids.add(pendentes.getString(1));
        }
        try (var atualizar = conexao.prepareStatement(
                "UPDATE conta SET numero_publico = ? WHERE id = ? AND numero_publico IS NULL")) {
            for (String id : ids) {
                atualizar.setLong(1, proximoNumero++);
                atualizar.setString(2, id);
                atualizar.executeUpdate();
            }
        }
        comando.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_conta_numero_publico ON conta(numero_publico)");
        comando.execute("INSERT OR IGNORE INTO conta_numero_seq (id, proximo) "
                + "SELECT 1, COALESCE(MAX(numero_publico), 0) + 1 FROM conta");
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
