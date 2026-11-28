package br.edu.puccampinas.es.expenseguard;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.puccampinas.es.expenseguard.application.ExpenseAnomalyService;
import br.edu.puccampinas.es.expenseguard.domain.decorator.AuditedDetectorDecorator;
import br.edu.puccampinas.es.expenseguard.domain.exception.ExternalIntegrationException;
import br.edu.puccampinas.es.expenseguard.domain.factory.StrictProfileDetectorFactory;
import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.ExpenseCategory;
import br.edu.puccampinas.es.expenseguard.domain.model.RiskLevel;
import br.edu.puccampinas.es.expenseguard.domain.port.AlertDispatcher;
import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import br.edu.puccampinas.es.expenseguard.infrastructure.persistence.FirebaseFirestoreExpenseAdapter;
import br.edu.puccampinas.es.expenseguard.infrastructure.resilience.ResilientAlertDispatcher;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExpenseAnomalyServiceTest {
    @Test
    void shouldDetectOutlierExpenseUsingStrategyAndDecorator() {
        FirebaseFirestoreExpenseAdapter repository = new FirebaseFirestoreExpenseAdapter();
        List<AnomalyDetectorStrategy> strategies = new StrictProfileDetectorFactory().createDetectors()
            .stream().map(s -> (AnomalyDetectorStrategy) new AuditedDetectorDecorator(s)).toList();
        ExpenseAnomalyService service = new ExpenseAnomalyService(repository, repository, alert -> {}, strategies);
        LocalDateTime start = LocalDateTime.now().minusDays(6);
        for (int i = 0; i < 5; i++) {
            service.processExpense(new Expense("base-" + i,
            "u1",
            "Refeicao",
            new BigDecimal(String.valueOf(48 + i)),
            ExpenseCategory.ALIMENTACAO,
            start.plusDays(i)));
        }
        Expense outlier = new Expense("outlier-1",
            "u1",
            "Jantar fora do padrao",
            new BigDecimal("950.00"),
            ExpenseCategory.ALIMENTACAO,
            LocalDateTime.now());
        List<AnomalyAlert> alerts = service.processExpense(outlier);
        assertFalse(alerts.isEmpty());
        assertEquals(6, repository.countSyncedDocuments());
    }

    @Test
    void shouldOpenCircuitBreakerAfterRepeatedExternalFailures() {
        AlertDispatcher offlineFirebase = alert -> {
            throw new RuntimeException("Firebase indisponivel");
        };
        ResilientAlertDispatcher dispatcher = new ResilientAlertDispatcher(offlineFirebase,
            2,
            100L,
            1,
            Duration.ofMinutes(5));
        AnomalyAlert sample = new AnomalyAlert("e1",
            "u1",
            "ZScore",
            RiskLevel.ALTO,
            4.2,
            "Teste",
            LocalDateTime.now());
        assertThrows(ExternalIntegrationException.class, () -> dispatcher.dispatch(sample));
        assertThrows(ExternalIntegrationException.class, () -> dispatcher.dispatch(sample));
    }

    @Test
    void shouldSaveExpenseEvenWhenAlertDispatchFails() {
        FirebaseFirestoreExpenseAdapter repository = new FirebaseFirestoreExpenseAdapter();
        AlertDispatcher circuitOpen = alert -> {
            throw new ExternalIntegrationException("Circuit Breaker ABERTO");
        };
        List<AnomalyDetectorStrategy> strategies = new StrictProfileDetectorFactory().createDetectors();
        ExpenseAnomalyService service = new ExpenseAnomalyService(repository, repository, circuitOpen, strategies);
        LocalDateTime start = LocalDateTime.now().minusDays(6);
        for (int i = 0; i < 5; i++) {
            service.processExpense(new Expense("base-" + i,
                "u1",
                "Refeicao",
                new BigDecimal(String.valueOf(48 + i)),
                ExpenseCategory.ALIMENTACAO,
                start.plusDays(i)));
        }
        Expense outlier = new Expense("outlier-2",
            "u1",
            "Jantar fora do padrao",
            new BigDecimal("950.00"),
            ExpenseCategory.ALIMENTACAO,
            LocalDateTime.now());
        List<AnomalyAlert> alerts = assertDoesNotThrow(() -> service.processExpense(outlier));
        assertFalse(alerts.isEmpty());
        assertEquals(6, repository.countSyncedDocuments());
    }
}
