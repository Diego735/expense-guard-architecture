package br.edu.puccampinas.es.expenseguard.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record AnomalyAlert(String expenseId,
            String userId,
            String detectorName,
            RiskLevel riskLevel,
            double anomalyScore,
            String explanation,
            LocalDateTime detectedAt) {
    public AnomalyAlert {
        Objects.requireNonNull(expenseId, "expenseId nao pode ser nulo");
        Objects.requireNonNull(userId, "userId nao pode ser nulo");
        Objects.requireNonNull(detectorName, "detectorName nao pode ser nulo");
        Objects.requireNonNull(riskLevel, "riskLevel nao pode ser nulo");
        Objects.requireNonNull(explanation, "explanation nao pode ser nulo");
        detectedAt = (detectedAt == null) ? LocalDateTime.now() : detectedAt;
    }
}
