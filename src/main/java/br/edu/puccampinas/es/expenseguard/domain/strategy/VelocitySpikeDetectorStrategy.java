package br.edu.puccampinas.es.expenseguard.domain.strategy;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.RiskLevel;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class VelocitySpikeDetectorStrategy implements AnomalyDetectorStrategy {
    private final int maxTransactionsInWindow;
    private final Duration windowDuration;

    public VelocitySpikeDetectorStrategy(int maxTransactionsInWindow, Duration windowDuration) {
        this.maxTransactionsInWindow = maxTransactionsInWindow;
        this.windowDuration = windowDuration;
    }

    @Override
    public String getStrategyName() { return "VelocitySpikeDetector"; }

    @Override
    public Optional<AnomalyAlert> analyze(Expense candidate, List<Expense> history) {
        if (history == null || history.isEmpty()) { return Optional.empty(); }
        LocalDateTime windowStart = candidate.getOccurredAt().minus(windowDuration);
        long recentCount = history.stream().filter(i -> !i.getOccurredAt().isBefore(windowStart)
                && !i.getOccurredAt().isAfter(candidate.getOccurredAt())).count();
        if (recentCount < maxTransactionsInWindow) { return Optional.empty(); }
        String explanation = String.format("Rajada detectada: %d transacoes em %d minutos.",
            recentCount + 1,
            windowDuration.toMinutes());
        return Optional.of(new AnomalyAlert(candidate.getId(),
            candidate.getUserId(),
            getStrategyName(),
            RiskLevel.ALTO,
            recentCount + 1,
            explanation,
            LocalDateTime.now()));
    }
}
