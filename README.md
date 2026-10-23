# Sistema Bancário com Transações Concorrentes

Projeto acadêmico em Java puro para demonstrar regras bancárias, race condition, sincronização, transferências sem deadlock, persistência JDBC com SQLite e exportação de extrato. A implementação mantém domínio, aplicação, persistência e simulação em pacotes separados.

## Pré-requisitos

- JDK 21 (o alvo de compilação Maven é `--release 21`).
- Apache Maven 3.9 ou superior.
- O driver SQLite JDBC é baixado pelo Maven; não é necessário instalar um servidor de banco.

No macOS com Homebrew, se necessário:

```bash
brew install openjdk@21 maven
```

Configure o JDK 21 como `JAVA_HOME` no terminal/IDE e confirme com `java -version` e `mvn -version`. Também é possível compilar com um JDK mais novo que aceite `--release 21`; a execução validada deve ser feita com JDK 21 quando disponível.

## Executar a demonstração

Na raiz do projeto:

```bash
mvn clean compile exec:java
```

A demonstração cria dados fictícios novos a cada execução. O banco padrão é `data/banco.db`. Para usar outro caminho:

```bash
mvn -Dbanco.db.path=/caminho/para/banco.db exec:java
```

O programa mostra depósito, saque, transferência, rejeição por saldo insuficiente, comparação da corrida controlada e depósitos/transferências concorrentes sincronizados. O cenário inseguro usa uma classe isolada e latches para garantir que as threads leiam o mesmo saldo antes de gravar.

## Testes

```bash
mvn clean test
```

Os testes criam bancos SQLite temporários e independentes. Cobrem regras de conta, persistência e reabertura do banco, ausência de registros para operações rejeitadas, depósitos concorrentes, transferências opostas sem deadlock e a race condition controlada.

## Banco e artefatos

- Banco SQLite padrão: `data/banco.db` (pode ser configurado por `-Dbanco.db.path=...`).
- Script de tabelas: `src/main/resources/db/schema.sql`; é executado automaticamente na inicialização.
- Extrato de exemplo: `docs/extrato-exemplo.txt`, gerado pela demonstração.
- Relatório com o resultado observado da corrida: `docs/relatorio-concorrencia.md`, gerado pela demonstração. Ele não contém valores presumidos: o programa grava o saldo esperado e o efetivamente observado.

As contas e os clientes são persistidos. Cada depósito, saque ou transferência grava o saldo alterado e a transação correspondente dentro do mesmo commit JDBC. Em falha de SQL, a operação é revertida e a conta em memória não é alterada. Valores monetários são armazenados como texto decimal para preservar a precisão de `BigDecimal`.

## Organização e requisitos

- `domain/`: `Cliente`, `Conta` e `Transacao`; `Conta` sincroniza alterações de saldo e adquire locks de transferência em ordem crescente de UUID.
- `service/`: `ServicoBancario` coordena validação, locks, persistência e geração de extrato/relatório.
- `repository/jdbc/`: conexão SQLite/JDBC e repositórios de clientes, contas e transações.
- `simulacao/`: implementação insegura isolada e cenários concorrentes controlados.
- `app/Main.java`: demonstração reproduzível pelo terminal.
- `src/test/java/`: testes unitários e de integração JUnit 5.

### Mapa dos requisitos do trabalho

| Requisito | Implementação |
|---|---|
| Clientes, contas e operações com `BigDecimal` | `domain/` e `service/ServicoBancario.java` |
| Race condition antes da correção | `simulacao/ContaInsegura.java` e `demonstrarRaceCondition` |
| Depósitos concorrentes seguros | métodos sincronizados de `Conta` e testes de `ServicoBancario` |
| Transferência oposta sem deadlock | locks por UUID em `Conta` e `ServicoBancario.transferir` |
| JDBC, tabelas e persistência atômica | `repository/jdbc/` e `src/main/resources/db/schema.sql` |
| Consulta e exportação do extrato | `TransacaoRepository` e `service/ExportadorExtrato.java` |
| Demonstração e artefatos | `app/Main.java`, `docs/extrato-exemplo.txt` e `docs/relatorio-concorrencia.md` |

## Limitações conhecidas

- O lock de Java protege operações dentro desta instância/processo. A atualização condicional do saldo e a transação SQLite protegem a persistência contra divergências observadas no banco; não há coordenação distribuída entre vários processos de aplicação.
- SQLite serializa escritas. `busy_timeout` ajuda a aguardar escritores concorrentes, mas um sistema bancário de produção exigiria banco servidor, observabilidade e políticas de retry.
- O demonstrador inseguro força deliberadamente uma leitura compartilhada para deixar a perda de atualização reproduzível; não mede a probabilidade de uma corrida em carga aleatória.
- As contas são mutáveis no domínio; para manter o banco sincronizado, as operações da aplicação devem passar por `ServicoBancario`.

## Evoluções futuras

Spring Boot, API REST, autenticação e AWS podem ser adicionados depois, mantendo as regras do domínio e trocando ou ampliando os adaptadores de persistência.
