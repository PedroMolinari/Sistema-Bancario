# Sistema Bancário

Aplicação acadêmica em Java para demonstrar operações bancárias, persistência SQLite e controle de concorrência. O projeto usa dados demonstrativos; não é um sistema financeiro de produção.

## Tecnologias e requisitos

- JDK 21 (o Maven compila com `--release 21`).
- Apache Maven 3.9 ou superior.
- Java Swing para a interface gráfica.
- SQLite via JDBC (`sqlite-jdbc`); não é necessário instalar um servidor de banco.
- JUnit 5 para os testes.

## Funcionalidades implementadas

- Cadastro conjunto de cliente e conta, listagem de contas, consulta de saldo e exclusão de contas sem movimentações.
- Depósito, saque e transferência entre contas, com validação de valores e saldo insuficiente.
- Extrato por conta e exportação do extrato em TXT.
- A visão geral busca no SQLite somente as seis movimentações mais recentes, sem carregar o extrato completo de cada conta.
- Demonstrações de race condition, depósitos concorrentes e transferências opostas.
- Interface Swing com navegação entre visão geral, contas, operações, extrato e chat informativo simulado.
- Acesso individual para clientes e perfil administrativo; clientes veem somente suas contas, e o administrador consulta a lista consolidada.
- Camada `AssistenteBancarioService` preparada para consultas de leitura; ainda não está ligada ao chat nem a um provedor de IA.

Não há filtro de extrato por período, busca textual de clientes/contas ou integração com Gemini. A aplicação demonstra dados fictícios e não deve ser usada para movimentar dinheiro real.

## Executar com dados isolados

### Demonstração de terminal

O demonstrador grava o banco e os arquivos gerados em `target/demo/` por padrão, sem usar `data/banco.db` nem substituir arquivos versionados em `docs/`. Execuções repetidas reutilizam esse banco isolado e acrescentam novas contas fictícias; os dois relatórios de saída são atualizados:

```bash
mvn -Dexec.mainClass=br.com.sistemabancario.app.Main exec:java
```

É possível escolher caminhos próprios com `-Dsistema.demo.dir=/caminho/isolado`. A propriedade `-Dbanco.db.path=/caminho/arquivo.db` substitui o caminho do banco principal da demonstração; evite apontá-la para bancos com dados que devam ser preservados. `-Dsistema.demo.simulacao.db.path=/caminho/arquivo.db` configura separadamente o banco da simulação concorrente.

### Interface Swing

A interface usa `data/banco.db` por padrão quando iniciada sem configuração. Para executar com um banco isolado dentro do diretório de build:

```bash
mvn -Dbanco.db.path=target/demo/interface.db -Dexec.mainClass=br.com.sistemabancario.ui.MainSwing exec:java
```

### Perfis de acesso

- Clientes criam usuário, senha e sua conta no formulário de acesso. Novas senhas precisam ter pelo menos 10 caracteres e são armazenadas como hash PBKDF2 com salt individual. Senhas já existentes continuam válidas para autenticação.
- Na primeira inicialização sem administrador, a tela de acesso oferece a configuração do perfil administrativo com nome, usuário e senha. O cadastro é fechado após a primeira criação; administradores adicionais continuam sujeitos à autorização administrativa.
- O perfil administrador não cria conta bancária automaticamente. Ele pode consultar a tabela geral, os saldos consolidados e excluir contas sem movimentações; operações financeiras continuam disponíveis em sessões de cliente.
- A autorização fica na fachada `ServicoBancarioAutorizado`, além dos controles da interface. O destino de uma transferência é informado por identificador, sem carregar saldos ou nomes de outros clientes.
- Contas criadas antes da autenticação não possuem credenciais vinculadas. Elas continuam preservadas e aparecem para o administrador, mas não podem ser acessadas por clientes até existir um processo administrativo de associação de acesso.
- As sessões existem somente na memória do processo e não expiram automaticamente. Este mecanismo é adequado à demonstração local, não a uma implantação financeira de produção.

Para iniciar a interface com banco isolado, passe um caminho SQLite temporário:

```bash
mvn -Dbanco.db.path=/tmp/sistema-bancario.db -Dexec.mainClass=br.com.sistemabancario.ui.MainSwing exec:java
```

Os dados em `target/demo/` são locais à demonstração. O comando `mvn clean` remove o diretório `target`; não o execute se precisar conservar esse banco demonstrativo.

## Testes e compilação

```bash
mvn test
mvn compile
```

Os testes de persistência usam bancos temporários (`@TempDir`) e não precisam acessar `data/banco.db`. `mvn test` preserva arquivos já existentes em `target`; `mvn clean` remove o conteúdo desse diretório.

## Persistência SQLite

- Caminho padrão: `data/banco.db`, configurável com a propriedade de sistema `banco.db.path`.
- O esquema está em `src/main/resources/db/schema.sql` e é inicializado pelo serviço ao abrir o banco.
- Os saldos são armazenados como texto decimal para preservar a precisão de `BigDecimal`.
- Cada depósito, saque ou transferência persiste os saldos e a transação no mesmo commit JDBC. Em falha de gravação, a transação é revertida antes de atualizar o estado em memória.
- As consultas e operações da interface são executadas em segundo plano com `SwingWorker`.

## Organização do projeto

```text
src/main/java/br/com/sistemabancario/
├── app/          demonstração de terminal
├── domain/       Cliente, Conta e Transacao
├── exception/    exceções de persistência
├── repository/   acesso SQLite/JDBC
├── service/      casos de uso bancários, exportação e consultas do assistente
├── simulacao/    cenários didáticos de concorrência
└── ui/           aplicação Swing, painéis e controlador
src/main/resources/db/schema.sql
src/test/java/    testes unitários, de persistência e de interface
```

O domínio define as entidades e validações. `ServicoBancario` coordena regras, sincronização e persistência; os repositórios executam consultas JDBC parametrizadas. `AutenticacaoService` emite sessões autenticadas, e `ServicoBancarioAutorizado` aplica o papel e a titularidade aos casos de uso usados pela interface. A porta do assistente é fornecida apenas para sessões de cliente e não contém operações de escrita.

## Limitações e cuidados

- O chat é simulado: responde a dúvidas gerais e não consulta contas nem realiza operações.
- A camada de consulta do assistente não envia dados a serviços externos, rejeita sessões administrativas e não oferece operações de escrita. A integração com IA ainda não foi implementada.
- A sincronização por locks protege operações dentro de uma instância da aplicação. Não existe coordenação distribuída entre vários processos usando o mesmo arquivo SQLite.
- SQLite serializa escritas; `busy_timeout` aguarda escritores concorrentes por tempo limitado. Um sistema de produção exigiria controles operacionais e uma arquitetura de persistência apropriada.
- O demonstrador inseguro força uma race condition para fins didáticos; não mede a frequência de falhas sob carga aleatória.
- `Conta` expõe métodos mutáveis no domínio. Para manter saldo em memória e persistência coerentes, operações da aplicação devem passar por `ServicoBancario`.

Os arquivos em `docs/` são artefatos existentes e não são substituídos pela demonstração atual, que grava seus resultados em `target/demo/`.
