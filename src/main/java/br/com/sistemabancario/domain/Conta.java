package br.com.sistemabancario.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Conta bancária cujas alterações de saldo são sincronizadas na JVM. */
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
    /** Forma compacta e reversível do UUID, estável e sem colisões entre contas. */
    public String getIdentificadorCurto() {
        return formatarIdentificador(identificador);
    }
    public static String formatarIdentificador(UUID identificador) {
        java.util.Objects.requireNonNull(identificador, "O identificador é obrigatório.");
        return new java.math.BigInteger(1, java.nio.ByteBuffer.allocate(16)
                .putLong(identificador.getMostSignificantBits())
                .putLong(identificador.getLeastSignificantBits()).array()).toString(36);
    }
    public Cliente getTitular() { return titular; }
    public synchronized BigDecimal getSaldo() { return saldo; }

    public synchronized void depositar(BigDecimal valor) {
        validarValorPositivo(valor);
        saldo = saldo.add(valor);
    }

    public synchronized void sacar(BigDecimal valor) {
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
        if (identificador.compareTo(destino.identificador) < 0) {
            synchronized (this) {
                synchronized (destino) {
                    transferirComBloqueios(destino, valor);
                }
            }
        } else {
            synchronized (destino) {
                synchronized (this) {
                    transferirComBloqueios(destino, valor);
                }
            }
        }
    }

    private void transferirComBloqueios(Conta destino, BigDecimal valor) {
        validarValorPositivo(valor);
        if (saldo.compareTo(valor) < 0) {
            throw new IllegalStateException("Saldo insuficiente para realizar a transferência.");
        }
        saldo = saldo.subtract(valor);
        destino.saldo = destino.saldo.add(valor);
    }

    private static void validarSaldoInicial(BigDecimal saldo) {
        Objects.requireNonNull(saldo, "O saldo inicial é obrigatório.");
        if (saldo.signum() < 0) {
            throw new IllegalArgumentException("O saldo inicial não pode ser negativo.");
        }
    }

    public static void validarValorPositivo(BigDecimal valor) {
        Objects.requireNonNull(valor, "O valor é obrigatório.");
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
    }
}
