package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdentificadoresContaUiTest {
    @Test
    void listasSeletoresEExtratoRenderizamIdsCurtosEEstaveis() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UUID origemId = UUID.fromString("12345678-1234-5678-9abc-def012345678");
            UUID destinoId = UUID.fromString("87654321-4321-8765-cba9-876543210fed");
            Conta origem = new Conta(origemId, new Cliente("Ana"), BigDecimal.TEN);
            Conta destino = new Conta(destinoId, new Cliente("Bia"), BigDecimal.ZERO);
            String origemCurto = origem.getIdentificadorCurto();
            assertEquals(origemCurto, Conta.formatarIdentificador(origemId));

            PainelContas painelContas = new PainelContas(() -> { }, (tela, conta) -> { }, conta -> { });
            painelContas.definirContas(List.of(origem, destino));
            JTable tabelaContas = encontrar(painelContas, JTable.class);
            assertEquals(origemCurto, tabelaContas.getModel().getValueAt(0, 1));
            assertNotEquals(origemId.toString(), tabelaContas.getModel().getValueAt(0, 1));

            PainelExtrato painelExtrato = new PainelExtrato(conta -> { }, (conta, arquivo) -> { });
            painelExtrato.definirContas(List.of(origem, destino));
            @SuppressWarnings("unchecked")
            JComboBox<Conta> seletor = (JComboBox<Conta>) encontrar(painelExtrato, JComboBox.class);
            JLabel renderizado = (JLabel) seletor.getRenderer().getListCellRendererComponent(
                    new JList<>(), origem, -1, false, false);
            assertTrue(renderizado.getText().contains(origemCurto));
            assertFalse(renderizado.getText().contains(origemId.toString()));

            painelExtrato.definirTransacoes(List.of(new Transacao(UUID.randomUUID(),
                    Transacao.Tipo.TRANSFERENCIA, BigDecimal.ONE, LocalDateTime.now(), origemId, destinoId)));
            JTable tabelaExtrato = encontrar(painelExtrato, JTable.class);
            assertEquals(origemCurto, tabelaExtrato.getModel().getValueAt(0, 3));
            assertEquals(destino.getIdentificadorCurto(), tabelaExtrato.getModel().getValueAt(0, 4));
        });
    }

    private static <T extends Component> T encontrar(Component raiz, Class<T> tipo) {
        T encontrado = procurar(raiz, tipo);
        if (encontrado == null) throw new AssertionError("Componente não encontrado: " + tipo.getSimpleName());
        return encontrado;
    }

    private static <T extends Component> T procurar(Component raiz, Class<T> tipo) {
        if (tipo.isInstance(raiz)) return tipo.cast(raiz);
        if (raiz instanceof java.awt.Container container) {
            for (Component filho : container.getComponents()) {
                T encontrado = procurar(filho, tipo);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }
}
