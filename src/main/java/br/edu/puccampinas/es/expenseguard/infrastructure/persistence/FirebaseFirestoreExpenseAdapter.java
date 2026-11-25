package br.edu.puccampinas.es.expenseguard.infrastructure.persistence;

import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.ExpenseCategory;
import br.edu.puccampinas.es.expenseguard.domain.port.ExpenseReadRepository;
import br.edu.puccampinas.es.expenseguard.domain.port.ExpenseWriteRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de persistencia compativel com o schema de documentos do Firebase Firestore
 * utilizado pelo Projeto Integrador (ver ADR-0006).
 */
public class FirebaseFirestoreExpenseAdapter implements ExpenseReadRepository, ExpenseWriteRepository {
    private final Map<String, Map<String, Object>> firestoreCollection = new LinkedHashMap<>();
    private final InMemoryExpenseRepository localCache = new InMemoryExpenseRepository();

    @Override
    public void save(Expense expense) {
        Map<String, Object> document = Map.of(
                "id", expense.getId(),
                "userId", expense.getUserId(),
                "description", expense.getDescription(),
                "amount", expense.getAmount().doubleValue(),
                "category", expense.getCategory().name(),
                "occurredAt", expense.getOccurredAt().toString());
        firestoreCollection.put(expense.getId(), document);
        localCache.save(expense);
    }

    @Override
    public List<Expense> findByUserAndCategory(String userId, ExpenseCategory category) {
        return localCache.findByUserAndCategory(userId, category);
    }

    @Override
    public List<Expense> findRecentByUser(String userId, int limit) {
        return localCache.findRecentByUser(userId, limit);
    }

    public int countSyncedDocuments() { return firestoreCollection.size(); }
}
