# ExpenseGuard — Motor de Deteccao de Anomalias em Despesas Pessoais

Sistema arquitetural desenvolvido em **Java 21** e **Maven** (PUC-Campinas) aplicando **Arquitetura Hexagonal**, **SOLID**, **Clean Code**, **Padroes GoF (Strategy, Factory Method, Decorator)** e **Resiliencia (Timeout, Retry, Circuit Breaker)** sobre o dominio do Projeto Integrador.

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
