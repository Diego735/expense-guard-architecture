package br.edu.puccampinas.es.expenseguard.domain.strategy;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.RiskLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ZScoreDetectorStrategy implements AnomalyDetectorStrategy {
    private static final int MIN_HISTORY_SIZE = 4;
    private final double zThreshold;

    public ZScoreDetectorStrategy(double zThreshold) { this.zThreshold = zThreshold; }

    @Override
    public String getStrategyName() { return "ZScoreStatisticalDetector"; }

    @Override
    public Optional<AnomalyAlert> analyze(Expense candidate, List<Expense> history) {
        if (history == null || history.size() < MIN_HISTORY_SIZE) { return Optional.empty(); }
        double mean = calculateMean(history);
        double stdDev = calculateStdDev(history, mean);
        if (stdDev == 0.0) { return Optional.empty(); }
        double zScore = (candidate.getAmount().doubleValue() - mean) / stdDev;
        if (zScore < zThreshold) { return Optional.empty(); }
        RiskLevel risk = zScore >= (zThreshold + 1.5) ? RiskLevel.CRITICO : RiskLevel.ALTO;
        String explanation = String.format("Valor R$ %.2f atingiu Z-Score %.2f (media R$ %.2f).",
            candidate.getAmount(),
            zScore,
            mean);
        return Optional.of(new AnomalyAlert(candidate.getId(),
            candidate.getUserId(),
            getStrategyName(),
            risk,
            zScore,
            explanation,
            LocalDateTime.now()));
    }

    private double calculateMean(List<Expense> expenses) {
        return expenses.stream().mapToDouble(item -> item.getAmount().doubleValue()).average().orElse(0.0);
    }

    private double calculateStdDev(List<Expense> expenses, double mean) {
        double variance = expenses.stream().mapToDouble(item -> Math.pow(item.getAmount().doubleValue() - mean,
            2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }
}
