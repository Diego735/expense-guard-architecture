package br.edu.puccampinas.es.expenseguard.domain.strategy;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import java.util.List;
import java.util.Optional;

public interface AnomalyDetectorStrategy {
    String getStrategyName();
    Optional<AnomalyAlert> analyze(Expense candidate, List<Expense> history);
}
