package br.com.sistemabancario.service;

import br.com.sistemabancario.repository.jdbc.ConexaoBanco;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class NumeroPublicoContaMigrationTest {
    @TempDir Path temporario;

    @Test
    void migraContasLegadasSemApagarDadosEAExecucaoEIdempotente() throws Exception {
        Path arquivo = temporario.resolve("legado.db");
        UUID clienteId = UUID.randomUUID();
        UUID contaId = UUID.randomUUID();
        try (Connection conexao = DriverManager.getConnection("jdbc:sqlite:" + arquivo);
             Statement sql = conexao.createStatement()) {
            sql.execute("CREATE TABLE cliente (id TEXT PRIMARY KEY NOT NULL, nome TEXT NOT NULL)");
            sql.execute("CREATE TABLE conta (id TEXT PRIMARY KEY NOT NULL, cliente_id TEXT NOT NULL, saldo TEXT NOT NULL)");
            sql.execute("INSERT INTO cliente VALUES ('" + clienteId + "', 'Ana legada')");
            sql.execute("INSERT INTO conta VALUES ('" + contaId + "', '" + clienteId + "', '42.75')");
        }

        ConexaoBanco banco = new ConexaoBanco(arquivo);
        banco.inicializar();
        banco.inicializar();
        var conta = new ServicoBancario(arquivo).obterConta(contaId);
        assertEquals(contaId, conta.getIdentificador());
        assertEquals("Ana legada", conta.getTitular().getNome());
        assertEquals(0, new BigDecimal("42.75").compareTo(conta.getSaldo()));
        assertEquals("000001", conta.getNumeroPublicoFormatado());
    }
}
