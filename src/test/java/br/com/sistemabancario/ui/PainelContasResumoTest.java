package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelContasResumoTest {
    @Test
    void tabelaConsolidadaEtotalRefletemAsContasEasAtualizacoesDeSaldo() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Conta primeira = new Conta(java.util.UUID.randomUUID(), new Cliente("Ana"),
                    new BigDecimal("120.50"), 123456);
            Conta segunda = new Conta(java.util.UUID.randomUUID(), new Cliente("Bia"),
                    new BigDecimal("79.50"), 123457);
            PainelContas painel = new PainelContas(() -> { }, (destino, conta) -> { }, conta -> { });

            painel.definirContas(List.of(primeira, segunda));
            JTable tabela = encontrarTabela(painel);
            assertEquals(2, tabela.getRowCount());
            assertEquals("Conta principal · 123456", tabela.getValueAt(0, 0));
            assertEquals("Ana", tabela.getValueAt(0, 1));
            assertEquals(EstiloSwing.moeda(primeira.getSaldo()), tabela.getValueAt(0, 2));
            assertTrue(contemTexto(painel, EstiloSwing.moeda(new BigDecimal("200.00"))));

            primeira.depositar(new BigDecimal("10.00"));
            painel.definirContas(List.of(primeira, segunda));
            assertEquals(EstiloSwing.moeda(primeira.getSaldo()), tabela.getValueAt(0, 2));
            assertTrue(contemTexto(painel, EstiloSwing.moeda(new BigDecimal("210.00"))));
        });
    }

    private static JTable encontrarTabela(java.awt.Container raiz) {
        for (java.awt.Component componente : raiz.getComponents()) {
            if (componente instanceof JTable tabela) return tabela;
            if (componente instanceof java.awt.Container container) {
                JTable tabela = encontrarTabela(container);
                if (tabela != null) return tabela;
            }
        }
        return null;
    }

    private static boolean contemTexto(java.awt.Container raiz, String texto) {
        for (java.awt.Component componente : raiz.getComponents()) {
            if (componente instanceof JLabel rotulo && texto.equals(rotulo.getText())) return true;
            if (componente instanceof java.awt.Container container && contemTexto(container, texto)) return true;
        }
        return false;
    }
}
