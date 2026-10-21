package br.com.sistemabancario.app;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.Conta;
import java.math.BigDecimal;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        Cliente ana = new Cliente("Ana Souza");
        Cliente bruno = new Cliente("Bruno Lima");
        Conta contaAna = new Conta(ana, new BigDecimal("1000.00"));
        Conta contaBruno = new Conta(bruno);

        contaAna.depositar(new BigDecimal("150.00"));
        contaAna.sacar(new BigDecimal("50.00"));
        contaAna.transferirPara(contaBruno, new BigDecimal("200.00"));

        System.out.println("Saldo de " + ana.getNome() + ": R$ " + contaAna.getSaldo());
        System.out.println("Saldo de " + bruno.getNome() + ": R$ " + contaBruno.getSaldo());
    }
}
