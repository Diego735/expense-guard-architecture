package br.edu.puccampinas.es.expenseguard;

import br.edu.puccampinas.es.expenseguard.application.ExpenseAnomalyService;
import br.edu.puccampinas.es.expenseguard.domain.factory.StrictProfileDetectorFactory;
import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.model.ExpenseCategory;
import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import br.edu.puccampinas.es.expenseguard.infrastructure.notification.ConsoleFirebaseAlertDispatcher;
import br.edu.puccampinas.es.expenseguard.infrastructure.persistence.FirebaseFirestoreExpenseAdapter;
import br.edu.puccampinas.es.expenseguard.infrastructure.resilience.ResilientAlertDispatcher;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseGuardApplication {
    public static void main(String[] args) {
        FirebaseFirestoreExpenseAdapter repository = new FirebaseFirestoreExpenseAdapter();
        ResilientAlertDispatcher dispatcher = new ResilientAlertDispatcher(
                new ConsoleFirebaseAlertDispatcher(), 3, 500L, 3, Duration.ofSeconds(30));
        List<AnomalyDetectorStrategy> detectors = new StrictProfileDetectorFactory().createAuditedDetectors();
        ExpenseAnomalyService service =
                new ExpenseAnomalyService(repository, repository, dispatcher, detectors);
        seedBaselineExpenses(service);
        Expense suspicious = new Expense(
                "exp-999",
                "user-diego",
                "Jantar de comemoracao fora do padrao",
                new BigDecimal("1850.00"),
                ExpenseCategory.ALIMENTACAO,
                LocalDateTime.now());
        List<AnomalyAlert> alerts = service.processExpense(suspicious);
        System.out.println("Total de anomalias disparadas: " + alerts.size());
    }

    private static void seedBaselineExpenses(ExpenseAnomalyService service) {
        LocalDateTime baseTime = LocalDateTime.now().minusDays(5);
        String[] amounts = {"42.00", "45.50", "39.90", "44.00", "41.20"};
        for (int i = 0; i < amounts.length; i++) {
            service.processExpense(new Expense(
                    "exp-" + i,
                    "user-diego",
                    "Almoco diario " + i,
                    new BigDecimal(amounts[i]),
                    ExpenseCategory.ALIMENTACAO,
                    baseTime.plusDays(i)));
        }
    }
}
