package br.com.sistemabancario.service;

import br.com.sistemabancario.simulacao.SimuladorConcorrencia;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RelatorioConcorrencia {
    public Path exportar(SimuladorConcorrencia.Resultado resultado, Path arquivo) {
        String conteudo = """
                # Demonstração de race condition

                ## Cenário inseguro

                A classe `ContaInsegura` lê o saldo sem sincronização. O simulador libera as gravações somente depois que todas as threads leram o mesmo saldo. Assim, cada depósito calcula o novo saldo a partir do mesmo valor e as gravações concorrentes se sobrescrevem.

                Saldo esperado: %s

                Saldo observado nesta execução: %s

                ## Correção

                A classe de produção `Conta` sincroniza depósitos e saques. O serviço mantém os locks das contas durante cada atualização de saldo e registro JDBC. Transferências adquirem os locks em ordem crescente de UUID; isso permite que operações opostas terminem sem ciclo de espera. A gravação do saldo e da transação usa uma transação SQLite com commit/rollback.

                O teste automatizado executa depósitos concorrentes e transferências opostas, aguarda todas as tarefas e compara saldos e invariantes. Esta demonstração controlada expõe a atualização perdida de forma reproduzível; não representa a frequência de falha de uma implementação insegura sem coordenação.
                """.formatted(resultado.esperado().toPlainString(), resultado.observado().toPlainString());
        try {
            Path destino = arquivo.toAbsolutePath().normalize();
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());
            Files.writeString(destino, conteudo, StandardCharsets.UTF_8);
            return destino;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gravar o relatório de concorrência.", e);
        }
    }
}
