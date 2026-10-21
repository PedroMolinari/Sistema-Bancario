package br.com.sistemabancario.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Operações sequenciais. A sincronização e a atomicidade entre contas serão tratadas em etapa futura. */
public final class Conta {
    private final UUID identificador;
    private final Cliente titular;
    private BigDecimal saldo;

    public Conta(Cliente titular) {
        this(UUID.randomUUID(), titular, BigDecimal.ZERO);
    }

    public Conta(Cliente titular, BigDecimal saldoInicial) {
        this(UUID.randomUUID(), titular, saldoInicial);
    }

    public Conta(UUID identificador, Cliente titular, BigDecimal saldoInicial) {
        this.identificador = Objects.requireNonNull(identificador, "O identificador da conta é obrigatório.");
        this.titular = Objects.requireNonNull(titular, "O titular da conta é obrigatório.");
        validarSaldoInicial(saldoInicial);
        this.saldo = saldoInicial;
    }

    public UUID getIdentificador() { return identificador; }
    public Cliente getTitular() { return titular; }
    public BigDecimal getSaldo() { return saldo; }

    public void depositar(BigDecimal valor) {
        validarValorPositivo(valor);
        saldo = saldo.add(valor);
    }

    public void sacar(BigDecimal valor) {
        validarValorPositivo(valor);
        if (saldo.compareTo(valor) < 0) {
            throw new IllegalStateException("Saldo insuficiente para realizar o saque.");
        }
        saldo = saldo.subtract(valor);
    }

    public void transferirPara(Conta destino, BigDecimal valor) {
        Objects.requireNonNull(destino, "A conta de destino é obrigatória.");
        if (this == destino || identificador.equals(destino.identificador)) {
            throw new IllegalArgumentException("A conta de origem e destino devem ser diferentes.");
        }
        validarValorPositivo(valor);
        if (saldo.compareTo(valor) < 0) {
            throw new IllegalStateException("Saldo insuficiente para realizar a transferência.");
        }
        // As verificações ocorrem antes de qualquer alteração; não oferece atomicidade concorrente.
        saldo = saldo.subtract(valor);
        destino.saldo = destino.saldo.add(valor);
    }

    private static void validarSaldoInicial(BigDecimal saldo) {
        Objects.requireNonNull(saldo, "O saldo inicial é obrigatório.");
        if (saldo.signum() < 0) {
            throw new IllegalArgumentException("O saldo inicial não pode ser negativo.");
        }
    }

    private static void validarValorPositivo(BigDecimal valor) {
        Objects.requireNonNull(valor, "O valor é obrigatório.");
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
    }
}
