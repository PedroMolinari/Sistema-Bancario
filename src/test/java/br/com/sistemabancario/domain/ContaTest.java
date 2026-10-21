package br.com.sistemabancario.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ContaTest {
    @Test
    void criaContaComSaldoInicialValido() {
        Conta conta = new Conta(new Cliente("Ana"), new BigDecimal("125.40"));
        assertEquals(0, new BigDecimal("125.40").compareTo(conta.getSaldo()));
    }

    @Test
    void realizaDepositoValido() {
        Conta conta = contaComSaldo("10.00");
        conta.depositar(new BigDecimal("2.35"));
        assertSaldo("12.35", conta);
    }

    @Test
    void realizaSaqueValido() {
        Conta conta = contaComSaldo("10.00");
        conta.sacar(new BigDecimal("3.25"));
        assertSaldo("6.75", conta);
    }

    @Test
    void rejeitaSaqueComSaldoInsuficiente() {
        Conta conta = contaComSaldo("5.00");
        assertThrows(IllegalStateException.class, () -> conta.sacar(new BigDecimal("5.01")));
        assertSaldo("5.00", conta);
    }

    @Test
    void rejeitaDepositoOuSaqueComValorInvalido() {
        Conta conta = contaComSaldo("10.00");
        assertThrows(NullPointerException.class, () -> conta.depositar(null));
        assertThrows(NullPointerException.class, () -> conta.sacar(null));
        for (BigDecimal valor : new BigDecimal[]{BigDecimal.ZERO, new BigDecimal("-1.00")}) {
            assertThrows(IllegalArgumentException.class, () -> conta.depositar(valor));
            assertThrows(IllegalArgumentException.class, () -> conta.sacar(valor));
        }
        assertSaldo("10.00", conta);
    }

    @Test
    void realizaTransferenciaValida() {
        Conta origem = contaComSaldo("20.00");
        Conta destino = contaComSaldo("3.00");
        origem.transferirPara(destino, new BigDecimal("7.50"));
        assertSaldo("12.50", origem);
        assertSaldo("10.50", destino);
    }

    @Test
    void rejeitaTransferenciasQueViolamRegras() {
        Conta origem = contaComSaldo("10.00");
        Conta destino = contaComSaldo("2.00");
        assertThrows(IllegalArgumentException.class, () -> origem.transferirPara(origem, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> origem.transferirPara(destino, BigDecimal.ZERO));
        assertThrows(IllegalStateException.class, () -> origem.transferirPara(destino, new BigDecimal("10.01")));
        assertThrows(NullPointerException.class, () -> origem.transferirPara(null, BigDecimal.ONE));
        assertSaldo("10.00", origem);
        assertSaldo("2.00", destino);
    }

    private static Conta contaComSaldo(String saldo) {
        return new Conta(new Cliente("Cliente"), new BigDecimal(saldo));
    }

    private static void assertSaldo(String esperado, Conta conta) {
        assertEquals(0, new BigDecimal(esperado).compareTo(conta.getSaldo()));
    }
}
