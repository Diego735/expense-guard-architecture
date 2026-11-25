package br.edu.puccampinas.es.expenseguard.domain.model;

import br.edu.puccampinas.es.expenseguard.domain.exception.InvalidExpenseException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Expense {
    private final String id;
    private final String userId;
    private final String description;
    private final BigDecimal amount;
    private final ExpenseCategory category;
    private final LocalDateTime occurredAt;

    public Expense(
            String id,
            String userId,
            String description,
            BigDecimal amount,
            ExpenseCategory category,
            LocalDateTime occurredAt) {
        validateTextFields(userId, description);
        validateFinancialFields(amount, category, occurredAt);
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.userId = userId.trim();
        this.description = description.trim();
        this.amount = amount;
        this.category = category;
        this.occurredAt = occurredAt;
    }

    private void validateTextFields(String userId, String description) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidExpenseException("Identificador do usuario e obrigatorio.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidExpenseException("Descricao da despesa e obrigatoria.");
        }
    }

    private void validateFinancialFields(
            BigDecimal amount, ExpenseCategory category, LocalDateTime occurredAt) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseException("Valor da despesa deve ser maior que zero.");
        }
        if (category == null) {
            throw new InvalidExpenseException("Categoria da despesa e obrigatoria.");
        }
        if (occurredAt == null) {
            throw new InvalidExpenseException("Data/hora da despesa e obrigatoria.");
        }
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Expense expense)) {
            return false;
        }
        return Objects.equals(id, expense.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
