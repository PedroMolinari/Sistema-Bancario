package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.service.ServicoBancario;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class FluxoCadastroUiTest {
    @TempDir Path diretorioTemporario;

    @Test
    void botaoNovoClienteAbreCartaoDeCadastro() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicReference<String> destino = new AtomicReference<>();
            PainelAreaRolavel telas = new PainelAreaRolavel(new CardLayout());
            PainelContas contas = new PainelContas(() -> { }, (tela, conta) -> destino.set(tela), conta -> { });
            PainelCadastroConta cadastro = new PainelCadastroConta((nome, saldo) -> { }, (nome, saldo) -> true);
            telas.registrar("contas", contas);
            telas.registrar("cadastro", cadastro);
            telas.mostrar("contas");

            encontrarBotao(contas, "Novo cliente e conta").doClick();
            assertEquals("cadastro", destino.get());
            telas.mostrar(destino.get());
            assertSame(cadastro, telaVisivel(telas));
        });
    }

    @Test
    void formularioCriaClienteEContaERegistrosPersistemAposReabrirBanco() throws Exception {
        Path arquivoTemporario = diretorioTemporario.resolve("cadastro-ui.sqlite");
        SwingUtilities.invokeAndWait(() -> {
            ServicoBancario servico = new ServicoBancario(arquivoTemporario);
            PainelContas painelContas = new PainelContas(() -> { }, (destino, conta) -> { }, conta -> { });
            PainelCadastroConta cadastro = new PainelCadastroConta(
                    (nome, saldo) -> {
                        servico.criarConta(new Cliente(nome), saldo);
                        painelContas.definirContas(servico.listarContas());
                    },
                    (nome, saldo) -> true);
            List<JTextField> campos = encontrarTodos(cadastro, JTextField.class);
            assertEquals(2, campos.size());
            campos.get(0).setText("Cliente de teste");
            campos.get(1).setText("123,45");
            encontrarBotao(cadastro, "Cadastrar cliente e conta").doClick();

            List<Conta> contas = servico.listarContas();
            assertEquals(1, contas.size());
            assertEquals("Cliente de teste", contas.get(0).getTitular().getNome());
            assertEquals(0, new BigDecimal("123.45").compareTo(contas.get(0).getSaldo()));
            JTable tabelaContas = encontrarTodos(painelContas, JTable.class).getFirst();
            assertEquals(1, tabelaContas.getRowCount());
            assertTrue(tabelaContas.getValueAt(0, 0).toString().startsWith("Conta principal"));
            assertEquals("Cliente de teste", tabelaContas.getValueAt(0, 1));
            UUID id = contas.get(0).getIdentificador();

            ServicoBancario reaberto = new ServicoBancario(arquivoTemporario);
            assertEquals(1, reaberto.listarContas().size());
            assertEquals(id, reaberto.listarContas().get(0).getIdentificador());
        });
    }

    private static JButton encontrarBotao(Component raiz, String texto) {
        return encontrarTodos(raiz, JButton.class).stream()
                .filter(botao -> texto.equals(botao.getText()))
                .findFirst().orElseThrow(() -> new AssertionError("Botão não encontrado: " + texto));
    }

    private static <T extends Component> List<T> encontrarTodos(Component raiz, Class<T> tipo) {
        java.util.ArrayList<T> encontrados = new java.util.ArrayList<>();
        coletar(raiz, tipo, encontrados);
        return encontrados;
    }

    private static <T extends Component> void coletar(Component raiz, Class<T> tipo, List<T> encontrados) {
        if (tipo.isInstance(raiz)) encontrados.add(tipo.cast(raiz));
        if (raiz instanceof Container container) {
            for (Component filho : container.getComponents()) coletar(filho, tipo, encontrados);
        }
    }

    private static Component telaVisivel(Container container) {
        for (Component filho : container.getComponents()) if (filho.isVisible()) return filho;
        throw new AssertionError("Nenhuma tela visível.");
    }
}
