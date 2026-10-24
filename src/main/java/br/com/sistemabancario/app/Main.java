package br.com.sistemabancario.app;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.service.ExportadorExtrato;
import br.com.sistemabancario.service.RelatorioConcorrencia;
import br.com.sistemabancario.service.ServicoBancario;
import br.com.sistemabancario.simulacao.SimuladorConcorrencia;
import java.math.BigDecimal;
import java.nio.file.Path;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        ServicoBancario banco = new ServicoBancario();
        Cliente ana = new Cliente("Ana Souza");
        Cliente bruno = new Cliente("Bruno Lima");
        Conta contaAna = banco.criarConta(ana, new BigDecimal("1000.00"));
        Conta contaBruno = banco.criarConta(bruno, BigDecimal.ZERO);

        banco.depositar(contaAna.getIdentificador(), new BigDecimal("150.00"));
        banco.sacar(contaAna.getIdentificador(), new BigDecimal("50.00"));
        banco.transferir(contaAna.getIdentificador(), contaBruno.getIdentificador(), new BigDecimal("200.00"));

        try {
            banco.sacar(contaAna.getIdentificador(), new BigDecimal("10000.00"));
        } catch (IllegalStateException e) {
            System.out.println("Saque rejeitado conforme esperado: " + e.getMessage());
        }

        SimuladorConcorrencia simulador = new SimuladorConcorrencia();
        var race = simulador.demonstrarRaceCondition(8);

        // Os movimentos artificiais da simulação ficam em outro arquivo SQLite e não entram no extrato demonstrativo.
        ServicoBancario bancoSimulacao = new ServicoBancario(Path.of("target/simulacao-concorrencia.db"));
        Conta contaSimulacaoA = bancoSimulacao.criarConta(
                new Cliente("Simulação concorrente A"), new BigDecimal("100.00"));
        Conta contaSimulacaoB = bancoSimulacao.criarConta(
                new Cliente("Simulação concorrente B"), new BigDecimal("100.00"));
        var corrigido = simulador.depositosConcorrentes(bancoSimulacao, contaSimulacaoA.getIdentificador(),
                4, 5, new BigDecimal("0.01"));
        var transferencias = simulador.transferenciasOpostas(bancoSimulacao,
                contaSimulacaoA.getIdentificador(), contaSimulacaoB.getIdentificador(),
                10, new BigDecimal("0.01"));

        Path relatorio = new RelatorioConcorrencia().exportar(race, Path.of("docs/relatorio-concorrencia.md"));
        Path extrato = new ExportadorExtrato(banco).exportar(
                contaAna.getIdentificador(), Path.of("docs/extrato-exemplo.txt"));

        System.out.println("Saldo esperado no cenário inseguro: R$ " + race.esperado());
        System.out.println("Saldo observado no cenário inseguro: R$ " + race.observado());
        System.out.println("Saldo esperado com depósitos sincronizados: R$ " + corrigido.esperado());
        System.out.println("Saldo observado com depósitos sincronizados: R$ " + corrigido.observado());
        System.out.println("Total antes/depois das transferências opostas: R$ "
                + transferencias.totalAntes() + " / R$ " + transferencias.totalDepois());
        System.out.println("Saldo de " + ana.getNome() + ": R$ " + contaAna.getSaldo());
        System.out.println("Saldo de " + bruno.getNome() + ": R$ " + contaBruno.getSaldo());
        System.out.println("Transações persistidas para Ana: " + banco.consultarExtrato(contaAna.getIdentificador()).size());
        System.out.println("Banco: " + banco.getArquivoBanco());
        System.out.println("Banco isolado da simulação: " + bancoSimulacao.getArquivoBanco());
        System.out.println("Extrato: " + extrato);
        System.out.println("Relatório: " + relatorio);
    }
}
