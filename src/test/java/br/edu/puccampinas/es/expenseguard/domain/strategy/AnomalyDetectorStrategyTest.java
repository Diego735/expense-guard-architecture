package br.edu.puccampinas.es.expenseguard.domain.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.puccampinas.es.expenseguard.domain.decorator.AuditedDetectorDecorator;
import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.ExpenseCategory;
import br.edu.puccampinas.es.expenseguard.domain.model.RiskLevel;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AnomalyDetectorStrategyTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 20, 12, 0);

    private static Expense expense(String id, String amount, LocalDateTime when) {
        return new Expense(id, "u1", "Despesa " + id, new BigDecimal(amount), ExpenseCategory.ALIMENTACAO, when);
    }

    private static List<Expense> dailyHistory(String... amounts) {
        List<Expense> history = new ArrayList<>();
        for (int i = 0; i < amounts.length; i++) {
            history.add(expense("h" + i, amounts[i], NOW.minusDays(amounts.length - i)));
        }
        return history;
    }

    @Test
    void zScoreShouldIgnoreWhenHistoryIsTooSmall() {
        ZScoreDetectorStrategy detector = new ZScoreDetectorStrategy(2.0);
        List<Expense> history = dailyHistory("40.00", "42.00", "41.00");
        assertTrue(detector.analyze(expense("c", "900.00", NOW), history).isEmpty());
    }

    @Test
    void zScoreShouldFlagValueFarAboveMean() {
        ZScoreDetectorStrategy detector = new ZScoreDetectorStrategy(2.0);
        List<Expense> history = dailyHistory("40.00", "42.00", "41.00", "43.00");
        Optional<AnomalyAlert> alert = detector.analyze(expense("c", "900.00", NOW), history);
        assertTrue(alert.isPresent());
        assertEquals(RiskLevel.CRITICO, alert.get().riskLevel());
    }

    @Test
    void iqrShouldNotFlagValueInsideFence() {
        IqrOutlierDetectorStrategy detector = new IqrOutlierDetectorStrategy();
        List<Expense> history = dailyHistory("40.00", "45.00", "50.00", "55.00", "60.00");
        assertTrue(detector.analyze(expense("c", "70.00", NOW), history).isEmpty());
    }

    @Test
    void iqrShouldFlagValueAboveUpperFence() {
        IqrOutlierDetectorStrategy detector = new IqrOutlierDetectorStrategy();
        List<Expense> history = dailyHistory("40.00", "45.00", "50.00", "55.00", "60.00");
        assertTrue(detector.analyze(expense("c", "300.00", NOW), history).isPresent());
    }

    @Test
    void velocityShouldFlagBurstInsideWindow() {
        VelocitySpikeDetectorStrategy detector = new VelocitySpikeDetectorStrategy(3, Duration.ofMinutes(30));
        List<Expense> burst = List.of(
            expense("b1", "20.00", NOW.minusMinutes(20)),
            expense("b2", "20.00", NOW.minusMinutes(10)),
            expense("b3", "20.00", NOW.minusMinutes(5)));
        Optional<AnomalyAlert> alert = detector.analyze(expense("c", "20.00", NOW), burst);
        assertTrue(alert.isPresent());
        assertEquals(RiskLevel.ALTO, alert.get().riskLevel());
    }

    @Test
    void velocityShouldIgnoreExpensesOutsideWindow() {
        VelocitySpikeDetectorStrategy detector = new VelocitySpikeDetectorStrategy(3, Duration.ofMinutes(30));
        List<Expense> spread = List.of(
            expense("b1", "20.00", NOW.minusHours(3)),
            expense("b2", "20.00", NOW.minusHours(2)),
            expense("b3", "20.00", NOW.minusMinutes(5)));
        assertTrue(detector.analyze(expense("c", "20.00", NOW), spread).isEmpty());
    }

    @Test
    void auditedDecoratorShouldKeepResultAndRecordLog() {
        AuditedDetectorDecorator audited = new AuditedDetectorDecorator(new ZScoreDetectorStrategy(2.0));
        List<Expense> history = dailyHistory("40.00", "42.00", "41.00", "43.00");
        Optional<AnomalyAlert> alert = audited.analyze(expense("c", "900.00", NOW), history);
        assertTrue(alert.isPresent());
        assertEquals(1, audited.getAuditLogs().size());
        assertFalse(audited.getAuditLogs().get(0).isBlank());
        assertTrue(audited.getAuditLogs().get(0).contains("status=ANOMALIA_DETECTADA"));
    }
}
