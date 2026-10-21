package br.com.sistemabancario.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Registro imutável de uma operação concluída; sua criação não executa nem persiste a operação. */
public record Transacao(UUID identificador, Tipo tipo, BigDecimal valor, LocalDateTime dataHora,
                        UUID contaOrigem, UUID contaDestino) {
    public Transacao {
        Objects.requireNonNull(identificador, "O identificador é obrigatório.");
        Objects.requireNonNull(tipo, "O tipo da transação é obrigatório.");
        Objects.requireNonNull(valor, "O valor é obrigatório.");
        Objects.requireNonNull(dataHora, "A data e hora são obrigatórias.");
        if (valor.signum() <= 0) throw new IllegalArgumentException("O valor deve ser maior que zero.");
        Objects.requireNonNull(contaOrigem, "A conta de origem é obrigatória.");
        if (tipo == Tipo.TRANSFERENCIA) {
            Objects.requireNonNull(contaDestino, "A conta de destino é obrigatória para transferência.");
            if (contaOrigem.equals(contaDestino)) throw new IllegalArgumentException("As contas devem ser diferentes.");
        } else if (contaDestino != null) {
            throw new IllegalArgumentException("Somente transferências possuem conta de destino.");
        }
    }

    public enum Tipo { DEPOSITO, SAQUE, TRANSFERENCIA }
}
