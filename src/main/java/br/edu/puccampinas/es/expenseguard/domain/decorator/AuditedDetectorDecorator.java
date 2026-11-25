package br.edu.puccampinas.es.expenseguard.domain.decorator;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuditedDetectorDecorator extends DetectorDecorator {
    private final List<String> auditLogs = new ArrayList<>();

    public AuditedDetectorDecorator(AnomalyDetectorStrategy wrapped) { super(wrapped); }

    @Override
    public Optional<AnomalyAlert> analyze(Expense candidate, List<Expense> history) {
        long startNano = System.nanoTime();
        Optional<AnomalyAlert> result = super.analyze(candidate, history);
        long elapsedMicros = (System.nanoTime() - startNano) / 1_000;
        String status = result.isPresent() ? "ANOMALIA_DETECTADA" : "NORMAL";
        auditLogs.add(String.format("[AUDIT] detector=%s expenseId=%s status=%s durationUs=%d",
            getStrategyName(),
            candidate.getId(),
            status,
            elapsedMicros));
        return result;
    }

    public List<String> getAuditLogs() { return Collections.unmodifiableList(auditLogs); }
}
