# Sistema Bancário

Aplicação desktop desenvolvida em Java com o objetivo de simular operações bancárias e explorar a integração de inteligência artificial em um sistema financeiro.

O projeto foi desenvolvido como trabalho acadêmico, com foco na aplicação prática de conceitos de programação, organização de código, persistência de dados e integração com um modelo de linguagem executado localmente.

## Funcionalidades

- **Autenticação:** acesso ao sistema com perfis de administrador e cliente.
- **Visão geral:** painel com resumo das contas, saldo total e movimentações recentes.
- **Gerenciamento de contas:** visualização e gerenciamento das contas cadastradas.
- **Operações bancárias:** depósitos, saques e transferências.
- **Extrato bancário:** consulta ao histórico de movimentações.
- **Persistência de dados:** armazenamento das informações utilizando SQLite.
- **Assistente com IA:** interface conversacional para dúvidas gerais sobre conceitos bancários e funcionalidades do sistema.

## Inteligência artificial

O projeto conta com um assistente conversacional integrado a um modelo **Qwen 2.5 1.5B Instruct**, executado localmente por meio do **llama.cpp**.

A comunicação é realizada por uma API HTTP compatível com o formato da OpenAI, utilizando o `HttpClient` do Java.

O assistente foi desenvolvido para responder dúvidas gerais e fornecer informações sobre conceitos bancários. Ele não consulta saldos reais, não acessa diretamente as contas e não executa operações bancárias.

A execução local permite experimentar a integração com IA sem depender de uma API paga ou de serviços externos de inferência.

## Tecnologias utilizadas

- Java 21 (compatibilidade de compilação)
- Java Swing
- Maven
- SQLite
- JDBC
- Java HttpClient
- SwingWorker
- Qwen 2.5 1.5B Instruct
- llama.cpp
- JUnit 5
- Git e GitHub

## Organização do projeto

O projeto utiliza separação de responsabilidades para organizar a aplicação, incluindo componentes de interface, regras de negócio, persistência e integração com o assistente de IA.

## Como executar

### Pré-requisitos

- JDK compatível com Java 21 ou superior.
- Maven.
- llama.cpp compilado com suporte ao modelo GGUF.
- Arquivo do modelo Qwen 2.5 1.5B Instruct no formato GGUF.

### Inicialização

O projeto disponibiliza o script `Iniciar Sistema Bancario.command`, que automatiza a inicialização do servidor local do modelo e da aplicação.

É necessário ajustar os caminhos do executável `llama-server` e do modelo GGUF no script para corresponder ao ambiente local.

Após configurar os caminhos, execute o inicializador.

Também é possível iniciar a aplicação pelo Maven, desde que o servidor local do Qwen esteja disponível e configurado.

## Testes

Os testes automatizados são executados com:

```bash
mvn test
```

A suíte contempla testes das regras da aplicação, persistência e componentes do assistente.

## Objetivo acadêmico

O projeto surgiu a partir de uma proposta acadêmica que tinha como foco a implementação de inteligência artificial em uma aplicação. A partir disso, foi desenvolvido um sistema bancário que reúne funcionalidades financeiras e um assistente conversacional integrado a um modelo de IA local.

Além da integração com inteligência artificial, o desenvolvimento permitiu trabalhar conceitos de orientação a objetos, interfaces gráficas, banco de dados, concorrência, testes automatizados e organização de aplicações Java.

---

**Projeto acadêmico desenvolvido para fins de estudo e demonstração. Não representa um sistema bancário real.**