package br.edu.puccampinas.es.expenseguard.domain.decorator;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class DetectorDecorator implements AnomalyDetectorStrategy {
    protected final AnomalyDetectorStrategy wrapped;

    protected DetectorDecorator(AnomalyDetectorStrategy wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "Estrategia encapsulada e obrigatoria");
    }

    @Override
    public String getStrategyName() { return wrapped.getStrategyName(); }

    @Override
    public Optional<AnomalyAlert> analyze(Expense candidate, List<Expense> history) {
        return wrapped.analyze(candidate, history);
    }
}
