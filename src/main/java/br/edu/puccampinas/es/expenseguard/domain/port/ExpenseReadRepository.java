package br.edu.puccampinas.es.expenseguard.domain.port;

import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.ExpenseCategory;
import java.util.List;

public interface ExpenseReadRepository {
    List<Expense> findByUserAndCategory(String userId, ExpenseCategory category);
    List<Expense> findRecentByUser(String userId, int limit);
}
