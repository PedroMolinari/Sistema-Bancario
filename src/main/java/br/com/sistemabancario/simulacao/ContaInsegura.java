package br.com.sistemabancario.simulacao;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

/** Implementação deliberadamente incorreta, usada apenas para visualizar atualização perdida. */
public final class ContaInsegura {
    private BigDecimal saldo = BigDecimal.ZERO;

    public void depositarSemSincronizacao(BigDecimal valor, CountDownLatch todosLeram,
                                          CountDownLatch liberarGravacoes) throws InterruptedException {
        BigDecimal saldoLido = saldo;
        todosLeram.countDown();
        liberarGravacoes.await();
        saldo = saldoLido.add(valor);
    }

    public BigDecimal getSaldo() { return saldo; }
}
