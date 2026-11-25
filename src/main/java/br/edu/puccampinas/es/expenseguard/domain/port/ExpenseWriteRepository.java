package br.edu.puccampinas.es.expenseguard.domain.port;

import br.edu.puccampinas.es.expenseguard.domain.model.Expense;

public interface ExpenseWriteRepository {
    void save(Expense expense);
}
