package br.com.sistemabancario.simulacao;

import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.service.ServicoBancario;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Cenários reproduzíveis de atualização perdida, depósitos sincronizados e transferências opostas. */
public final class SimuladorConcorrencia {
    public record Resultado(BigDecimal esperado, BigDecimal observado) { }
    public record ResultadoTransferencias(BigDecimal saldoOrigem, BigDecimal saldoDestino,
                                          BigDecimal totalAntes, BigDecimal totalDepois) { }

    public Resultado demonstrarRaceCondition(int quantidadeThreads) {
        if (quantidadeThreads < 2) throw new IllegalArgumentException("Use pelo menos duas threads.");
        ContaInsegura conta = new ContaInsegura();
        CountDownLatch prontas = new CountDownLatch(quantidadeThreads);
        CountDownLatch iniciar = new CountDownLatch(1);
        CountDownLatch todasLeramSaldo = new CountDownLatch(quantidadeThreads);
        CountDownLatch liberarGravacoes = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(quantidadeThreads);
        List<Future<?>> tarefas = new ArrayList<>();
        try {
            for (int i = 0; i < quantidadeThreads; i++) {
                tarefas.add(executor.submit(() -> {
                    prontas.countDown();
                    aguardar(iniciar);
                    try {
                        conta.depositarSemSincronizacao(BigDecimal.ONE, todasLeramSaldo, liberarGravacoes);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Thread interrompida.", e);
                    }
                }));
            }
            aguardar(prontas);
            iniciar.countDown();
            aguardar(todasLeramSaldo);
            liberarGravacoes.countDown();
            concluir(tarefas);
            return new Resultado(BigDecimal.valueOf(quantidadeThreads), conta.getSaldo());
        } finally {
            executor.shutdownNow();
        }
    }

    public Resultado depositosConcorrentes(ServicoBancario banco, UUID contaId,
                                            int quantidadeThreads, int depositosPorThread,
                                            BigDecimal valor) {
        validarCargas(quantidadeThreads, depositosPorThread);
        Conta conta = banco.obterConta(contaId);
        BigDecimal saldoInicial = conta.getSaldo();
        CountDownLatch prontas = new CountDownLatch(quantidadeThreads);
        CountDownLatch iniciar = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(quantidadeThreads);
        List<Future<?>> tarefas = new ArrayList<>();
        try {
            for (int i = 0; i < quantidadeThreads; i++) {
                tarefas.add(executor.submit(() -> {
                    prontas.countDown();
                    aguardar(iniciar);
                    for (int operacao = 0; operacao < depositosPorThread; operacao++) {
                        banco.depositar(contaId, valor);
                    }
                }));
            }
            aguardar(prontas);
            iniciar.countDown();
            concluir(tarefas);
            BigDecimal esperado = saldoInicial.add(valor.multiply(
                    BigDecimal.valueOf((long) quantidadeThreads * depositosPorThread)));
            return new Resultado(esperado, banco.obterConta(contaId).getSaldo());
        } finally {
            executor.shutdownNow();
        }
    }

    public ResultadoTransferencias transferenciasOpostas(ServicoBancario banco, UUID contaA,
                                                           UUID contaB, int transferenciasPorThread,
                                                           BigDecimal valor) {
        if (transferenciasPorThread <= 0) throw new IllegalArgumentException("A quantidade deve ser positiva.");
        Conta a = banco.obterConta(contaA);
        Conta b = banco.obterConta(contaB);
        BigDecimal totalAntes = a.getSaldo().add(b.getSaldo());
        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<?>> tarefas = List.of(
                executor.submit(() -> transferirEmLaco(banco, contaA, contaB, transferenciasPorThread, valor, prontas, iniciar)),
                executor.submit(() -> transferirEmLaco(banco, contaB, contaA, transferenciasPorThread, valor, prontas, iniciar)));
        try {
            aguardar(prontas);
            iniciar.countDown();
            concluir(tarefas);
            BigDecimal saldoA = banco.obterConta(contaA).getSaldo();
            BigDecimal saldoB = banco.obterConta(contaB).getSaldo();
            return new ResultadoTransferencias(saldoA, saldoB, totalAntes, saldoA.add(saldoB));
        } finally {
            executor.shutdownNow();
        }
    }

    private static void transferirEmLaco(ServicoBancario banco, UUID origem, UUID destino,
                                          int quantidade, BigDecimal valor,
                                          CountDownLatch prontas, CountDownLatch iniciar) {
        prontas.countDown();
        aguardar(iniciar);
        for (int i = 0; i < quantidade; i++) banco.transferir(origem, destino, valor);
    }

    private static void validarCargas(int threads, int operacoes) {
        if (threads <= 0 || operacoes <= 0) throw new IllegalArgumentException("Threads e operações devem ser positivas.");
    }

    private static void aguardar(CountDownLatch latch) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) throw new IllegalStateException("Tempo esgotado aguardando threads.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread coordenadora interrompida.", e);
        }
    }

    private static void concluir(List<Future<?>> tarefas) {
        try {
            for (Future<?> tarefa : tarefas) tarefa.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Uma operação concorrente falhou ou excedeu o tempo limite.", e);
        }
    }
}
