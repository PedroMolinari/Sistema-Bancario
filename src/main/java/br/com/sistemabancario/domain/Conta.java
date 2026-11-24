package br.com.sistemabancario.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Conta bancária cujas alterações de saldo são sincronizadas na JVM. */
public final class Conta {
    private final UUID identificador;
    private final Cliente titular;
    private BigDecimal saldo;
    private long numeroPublico;

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

    public Conta(UUID identificador, Cliente titular, BigDecimal saldoInicial, long numeroPublico) {
        this(identificador, titular, saldoInicial);
        atribuirNumeroPublico(numeroPublico);
    }

    public UUID getIdentificador() { return identificador; }
    /** Número público persistido, separado do UUID interno usado por relações e transações. */
    public long getNumeroPublico() { return numeroPublico; }
    public String getNumeroPublicoFormatado() { return formatarNumeroPublico(numeroPublico); }
    public String getNomePublico() {
        return numeroPublico > 0 ? "Conta principal · " + getNumeroPublicoFormatado() : "Conta principal";
    }
    public static String formatarNumeroPublico(long numero) {
        if (numero <= 0) throw new IllegalArgumentException("O número público da conta é inválido.");
        return String.format(java.util.Locale.ROOT, "%06d", numero);
    }
    /** Atribuído pelo repositório na criação; não altera o UUID nem o saldo da conta. */
    public void atribuirNumeroPublico(long numero) {
        if (numero <= 0) throw new IllegalArgumentException("O número público da conta é inválido.");
        numeroPublico = numero;
    }
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

    public static UUID decodificarIdentificador(String identificadorCurto) {
        Objects.requireNonNull(identificadorCurto, "O identificador da conta é obrigatório.");
        try {
            java.math.BigInteger valor = new java.math.BigInteger(identificadorCurto.trim(), 36);
            if (valor.signum() < 0 || valor.bitLength() > 128) throw new NumberFormatException();
            byte[] bytes = new byte[16];
            byte[] magnitude = valor.toByteArray();
            int origem = magnitude.length > 16 ? magnitude.length - 16 : 0;
            int tamanho = magnitude.length - origem;
            System.arraycopy(magnitude, origem, bytes, 16 - tamanho, tamanho);
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
            return new UUID(buffer.getLong(), buffer.getLong());
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("O identificador curto da conta é inválido.", erro);
        }
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
