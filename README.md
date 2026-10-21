# Sistema Bancário

Base de uma aplicação bancária independente para estudo de orientação a objetos. Esta etapa implementa somente regras sequenciais no domínio; persistência JDBC/H2, concorrência e API ficam para etapas posteriores.

## Pré-requisitos

- JDK 21 (ou superior compatível para compilar o alvo 21)
- Apache Maven 3.9 ou superior

## Executar

Na raiz do projeto:

```bash
mvn clean compile exec:java
```

## Testes

```bash
mvn test
```

## Organização

- `domain`: cliente, conta e representação imutável de transação.
- `app`: demonstração sequencial em `Main`.
- `src/test`: testes unitários JUnit 5 para as regras de `Conta`.

Os valores monetários usam `BigDecimal`. A conta não expõe um setter de saldo. O registro de uma `Transacao` é independente da execução das operações: nesta base, as operações não geram nem persistem histórico, pois isso deverá ser orquestrado por um serviço com persistência nas etapas seguintes.

As operações ainda não são seguras para acesso concorrente. A transferência valida as regras e atualiza as duas contas sequencialmente, sem sincronização ou garantia de atomicidade entre threads. A implementação de concorrência será feita em etapa própria.
