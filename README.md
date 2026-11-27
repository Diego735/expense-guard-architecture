# ExpenseGuard — Motor de Deteccao de Anomalias em Despesas Pessoais

Sistema arquitetural desenvolvido em **Java 21** e **Maven** PUC-Campinas aplicando **Arquitetura Hexagonal**, **SOLID**, **Clean Code**, **Padroes GoF (Strategy, Factory Method, Decorator)** e **Resiliencia (Timeout, Retry, Circuit Breaker)** sobre o dominio do Projeto Integrador.

## Como Instalar, Validar e Executar

```bash
mvn spotless:check checkstyle:check test
mvn compile
java -cp target/classes br.edu.puccampinas.es.expenseguard.ExpenseGuardApplication
```

## Estrutura
- `src/main/java/.../domain/`: Modelos, Excecoes, Portas, Strategies, Factories e Decorators.
- `src/main/java/.../application/`: `ExpenseAnomalyService`.
- `src/main/java/.../infrastructure/`: `FirebaseFirestoreExpenseAdapter` e `ResilientAlertDispatcher`.
- `adrs/`: Registros de Decisao Arquitetural (`ADR-0001` a `ADR-0006`).
- `diagrams/`: Diagramas Mermaid.
- `docs/`: Relatorios de Qualidade, SOLID, Clean Code, GoF e Resiliencia.


## Comandos
## 4. Arquitetura, Principios e Padroes Aplicados

### Arquitetura Hexagonal (Ports & Adapters)
O projeto isola completamente as regras de negocio de frameworks e servicos externos:
- **Nucleo (`domain/` e `application/`):** Contem as entidades imutaveis (`Expense`, `AnomalyAlert`), regras de negocio e portas de entrada/saida (`ExpenseReadRepository`, `ExpenseWriteRepository`, `AlertDispatcher`). Nao possui dependencias de bancos de dados ou SDKs externos.
- **Adaptadores (`infrastructure/`):** Implementam as portas definidas pelo dominio (`FirebaseFirestoreExpenseAdapter` para persistencia compativel com documentos Firestore e `ConsoleFirebaseAlertDispatcher` para notificacoes).

### Principios SOLID
- **SRP (Single Responsibility Principle):** Separacao clara entre validacao de dominio (`Expense`), calculo estatistico (`ZScoreDetectorStrategy`) e orquestracao de caso de uso (`ExpenseAnomalyService`).
- **OCP (Open/Closed Principle):** Novos algoritmos de deteccao podem ser adicionados implementando `AnomalyDetectorStrategy` sem modificar o servico principal.
- **LSP (Liskov Substitution Principle):** Decoradores e estrategias concretas substituem `AnomalyDetectorStrategy` mantendo o mesmo contrato e comportamento esperado.
- **ISP (Interface Segregation Principle):** Operacoes de banco segregadas em `ExpenseReadRepository` (leitura de historico) e `ExpenseWriteRepository` (gravacao), evitando que detectores dependam de metodos de escrita.
- **DIP (Dependency Inversion Principle):** A camada de aplicacao (`ExpenseAnomalyService`) depende exclusivamente de abstracoes do pacote `domain`, recebendo as implementacoes por injecao de dependencia no construtor.

### Padroes Projeto GoF
1. **Strategy (`AnomalyDetectorStrategy`):** Encapsula tres algoritmos independentes de deteccao de anomalias, eliminando condicionais complexas:
   - `ZScoreDetectorStrategy`: Identifica valores que fogem drasticamente da media e do desvio padrao historico.
   - `IqrOutlierDetectorStrategy`: Detecta outliers com base no Intervalo Interquartil (Q1 e Q3), resistente a distorcoes extremas.
   - `VelocitySpikeDetectorStrategy`: Identifica rajadas de gastos atipicos em janelas curtas de tempo.
2. **Factory Method (`DetectorFactory`):** Padroniza a instanciacao e calibracao das familias de detectores de acordo com o perfil de risco do usuario (`StrictProfileDetectorFactory` e `StandardProfileDetectorFactory`).
3. **Decorator (`AuditedDetectorDecorator`):** Envolve dinamicamente qualquer `AnomalyDetectorStrategy` para registrar telemetria de tempo de execucao em microssegundos sem alterar o codigo dos algoritmos matematicos.

### Resiliencia e Qualidade de Codigo
- **Timeout, Retry e Circuit Breaker (`ResilientAlertDispatcher`):** Garante tolerancia a falhas no envio de alertas externos por meio de execucao assincrona com limite de tempo (`500ms`), retentativas automaticas (`3x`) e abertura de circuito apos falhas consecutivas.
- **Clean Code e Governanca:** Validacao automatizada no pipeline de CI (GitHub Actions) com **Spotless** (padronizacao de formatacao), **Checkstyle** (limites de 35 linhas por metodo, 4 parametros e 120 caracteres por linha) e testes unitarios com **JUnit 5**.
