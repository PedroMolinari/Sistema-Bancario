package br.com.sistemabancario.app;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @TempDir Path diretorioTemporario;

    @Test
    void demonstracaoGravaBancoESaidasNoDiretorioConfigurado() {
        String diretorioAnterior = System.getProperty("sistema.demo.dir");
        String bancoAnterior = System.getProperty("banco.db.path");
        Path diretorioDemo = diretorioTemporario.resolve("demo");
        try {
            System.setProperty("sistema.demo.dir", diretorioDemo.toString());
            System.clearProperty("banco.db.path");

            Main.main(new String[0]);

            assertTrue(Files.exists(diretorioDemo.resolve("banco.db")));
            assertTrue(Files.exists(diretorioDemo.resolve("simulacao-concorrencia.db")));
            assertTrue(Files.exists(diretorioDemo.resolve("extrato-exemplo.txt")));
            assertTrue(Files.exists(diretorioDemo.resolve("relatorio-concorrencia.md")));
        } finally {
            restaurarPropriedade("sistema.demo.dir", diretorioAnterior);
            restaurarPropriedade("banco.db.path", bancoAnterior);
        }
    }

    private static void restaurarPropriedade(String chave, String valor) {
        if (valor == null) System.clearProperty(chave);
        else System.setProperty(chave, valor);
    }
}
