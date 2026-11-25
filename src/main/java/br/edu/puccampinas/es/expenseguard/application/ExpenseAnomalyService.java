package br.edu.puccampinas.es.expenseguard.application;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.port.AlertDispatcher;
import br.edu.puccampinas.es.expenseguard.domain.port.ExpenseReadRepository;
import br.edu.puccampinas.es.expenseguard.domain.port.ExpenseWriteRepository;
import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import java.util.ArrayList;
import java.util.List;

public class ExpenseAnomalyService {
    private final ExpenseReadRepository readRepository;
    private final ExpenseWriteRepository writeRepository;
    private final AlertDispatcher alertDispatcher;
    private final List<AnomalyDetectorStrategy> detectors;

    public ExpenseAnomalyService(ExpenseReadRepository readRepository,
            ExpenseWriteRepository writeRepository,
            AlertDispatcher alertDispatcher,
            List<AnomalyDetectorStrategy> detectors) {
        this.readRepository = readRepository;
        this.writeRepository = writeRepository;
        this.alertDispatcher = alertDispatcher;
        this.detectors = List.copyOf(detectors);
    }

    public List<AnomalyAlert> processExpense(Expense expense) {
        List<Expense> categoryHistory = readRepository.findByUserAndCategory(expense.getUserId(),
            expense.getCategory());
        List<AnomalyAlert> triggeredAlerts = new ArrayList<>();
        for (AnomalyDetectorStrategy detector : detectors) {
            detector.analyze(expense, categoryHistory).ifPresent(alert -> {
                triggeredAlerts.add(alert);
                alertDispatcher.dispatch(alert);
            });
        }
        writeRepository.save(expense);
        return triggeredAlerts;
    }
}
