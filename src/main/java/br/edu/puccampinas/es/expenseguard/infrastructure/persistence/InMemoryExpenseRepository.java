package br.edu.puccampinas.es.expenseguard.infrastructure.persistence;

import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.ExpenseCategory;
import br.edu.puccampinas.es.expenseguard.domain.port.ExpenseReadRepository;
import br.edu.puccampinas.es.expenseguard.domain.port.ExpenseWriteRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryExpenseRepository implements ExpenseReadRepository, ExpenseWriteRepository {
    private final List<Expense> storage = new ArrayList<>();

    @Override
    public void save(Expense expense) {
        storage.add(expense);
    }

    @Override
    public List<Expense> findByUserAndCategory(String userId, ExpenseCategory category) {
        return storage.stream()
                .filter(item -> item.getUserId().equals(userId) && item.getCategory() == category)
                .toList();
    }

    @Override
    public List<Expense> findRecentByUser(String userId, int limit) {
        return storage.stream()
                .filter(item -> item.getUserId().equals(userId))
                .sorted(Comparator.comparing(Expense::getOccurredAt).reversed())
                .limit(limit)
                .toList();
    }
}
