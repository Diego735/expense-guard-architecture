package br.edu.puccampinas.es.expenseguard.domain.strategy;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.RiskLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class IqrOutlierDetectorStrategy implements AnomalyDetectorStrategy {
    private static final int MIN_SAMPLE_SIZE = 4;
    private static final double IQR_MULTIPLIER = 1.5;

    @Override
    public String getStrategyName() { return "IqrOutlierDetector"; }

    @Override
    public Optional<AnomalyAlert> analyze(Expense candidate, List<Expense> history) {
        if (history == null || history.size() < MIN_SAMPLE_SIZE) { return Optional.empty(); }
        List<Double> sorted = history.stream().map(item -> item.getAmount().doubleValue()).sorted().toList();
        double q1 = sorted.get(sorted.size() / 4);
        double q3 = sorted.get((sorted.size() * 3) / 4);
        double iqr = q3 - q1;
        double upperFence = q3 + (IQR_MULTIPLIER * iqr);
        double currentAmount = candidate.getAmount().doubleValue();
        if (iqr <= 0.0 || currentAmount <= upperFence) { return Optional.empty(); }
        double excessRatio = (currentAmount - upperFence) / iqr;
        RiskLevel risk = excessRatio > 2.0 ? RiskLevel.CRITICO : RiskLevel.MEDIO;
        String explanation = String.format("Despesa R$ %.2f ultrapassou limite IQR (R$ %.2f).",
            currentAmount,
            upperFence);
        return Optional.of(new AnomalyAlert(candidate.getId(),
            candidate.getUserId(),
            getStrategyName(),
            risk,
            excessRatio,
            explanation,
            LocalDateTime.now()));
    }
}
