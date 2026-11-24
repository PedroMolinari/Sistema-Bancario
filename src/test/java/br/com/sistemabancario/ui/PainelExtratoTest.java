package br.com.sistemabancario.ui;

import br.com.sistemabancario.domain.Transacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PainelExtratoTest {
    @Test
    void filtroDePeriodoIncluiAsDuasDatasLimiteERejeitaPeriodoInvertido() {
        UUID origem = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 2, 1);
        LocalDate fim = LocalDate.of(2026, 2, 3);
        List<Transacao> lista = List.of(
                transacao(origem, LocalDateTime.of(2026, 1, 31, 23, 59)),
                transacao(origem, LocalDateTime.of(2026, 2, 1, 0, 0)),
                transacao(origem, LocalDateTime.of(2026, 2, 3, 23, 59)),
                transacao(origem, LocalDateTime.of(2026, 2, 4, 0, 0)));

        List<Transacao> filtradas = PainelExtrato.filtrarPeriodo(lista, inicio, fim);
        assertEquals(2, filtradas.size());
        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 0), filtradas.getFirst().dataHora());
        assertThrows(IllegalArgumentException.class,
                () -> PainelExtrato.filtrarPeriodo(lista, fim, inicio));
    }

    @Test
    void controlesDeDataUsamSeletoresSwingFormatados() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PainelExtrato painel = new PainelExtrato(conta -> { }, (conta, arquivo) -> { });
            List<JSpinner> datas = encontrar(painel);
            assertEquals(2, datas.size());
            for (JSpinner data : datas) {
                assertInstanceOf(JSpinner.DateEditor.class, data.getEditor());
                assertEquals("dd/MM/yyyy", ((JSpinner.DateEditor) data.getEditor()).getFormat().toPattern());
                assertFalse(((JSpinner.DateEditor) data.getEditor()).getFormat().isLenient());
                assertNotNull(data.getValue());
            }
            List<JButton> calendarios = encontrarBotoes(painel);
            assertEquals(2, calendarios.size());
            for (JButton calendario : calendarios) {
                assertEquals("Abrir calendário", calendario.getAccessibleContext().getAccessibleName());
                assertNotNull(calendario.getIcon());
                assertEquals(34, calendario.getPreferredSize().width);
            }
        });
    }

    @Test
    void calendarioAtualizaDataESeletorRejeitaDataImpossivel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JSpinner data = PainelExtrato.criarSeletorData(LocalDate.of(2026, 1, 1));
            LocalDate escolhida = LocalDate.of(2026, 2, 28);
            PainelExtrato.aplicarData(data, escolhida);
            assertEquals(escolhida, PainelExtrato.dataEscolhida(data));

            javax.swing.JFormattedTextField texto = ((JSpinner.DateEditor) data.getEditor()).getTextField();
            texto.setText("31/02/2026");
            assertThrows(IllegalArgumentException.class, () -> PainelExtrato.dataEscolhida(data));
        });
    }

    private static Transacao transacao(UUID origem, LocalDateTime data) {
        return new Transacao(UUID.randomUUID(), Transacao.Tipo.DEPOSITO, BigDecimal.ONE, data, origem, null);
    }

    private static List<JSpinner> encontrar(java.awt.Component raiz) {
        java.util.ArrayList<JSpinner> resultado = new java.util.ArrayList<>();
        if (raiz instanceof JSpinner spinner) resultado.add(spinner);
        if (raiz instanceof java.awt.Container container) {
            for (java.awt.Component filho : container.getComponents()) resultado.addAll(encontrar(filho));
        }
        return resultado;
    }

    private static List<JButton> encontrarBotoes(java.awt.Component raiz) {
        java.util.ArrayList<JButton> resultado = new java.util.ArrayList<>();
        if (raiz instanceof JButton botao && "Abrir calendário".equals(
                botao.getAccessibleContext().getAccessibleName())) resultado.add(botao);
        if (raiz instanceof java.awt.Container container) {
            for (java.awt.Component filho : container.getComponents()) resultado.addAll(encontrarBotoes(filho));
        }
        return resultado;
    }
}
