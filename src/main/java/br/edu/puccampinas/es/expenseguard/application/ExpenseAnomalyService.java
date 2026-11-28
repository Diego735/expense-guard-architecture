package br.edu.puccampinas.es.expenseguard.application;

import br.edu.puccampinas.es.expenseguard.domain.exception.ExternalIntegrationException;
import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.model.Expense;
import br.edu.puccampinas.es.expenseguard.domain.port.AlertDispatcher;
import br.edu.puccampinas.es.expenseguard.domain.port.ExpenseReadRepository;
import br.edu.puccampinas.es.expenseguard.domain.port.ExpenseWriteRepository;
import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Optional;

public class ExpenseAnomalyService {
    private static final Logger LOGGER = System.getLogger(ExpenseAnomalyService.class.getName());

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
        List<AnomalyAlert> triggeredAlerts = detectAnomalies(expense, categoryHistory);
        writeRepository.save(expense);
        triggeredAlerts.forEach(this::dispatchWithoutBlocking);
        return triggeredAlerts;
    }

    private List<AnomalyAlert> detectAnomalies(Expense expense, List<Expense> history) {
        return detectors.stream()
                .map(detector -> detector.analyze(expense, history))
                .flatMap(Optional::stream)
                .toList();
    }

    private void dispatchWithoutBlocking(AnomalyAlert alert) {
        try {
            alertDispatcher.dispatch(alert);
        } catch (ExternalIntegrationException ex) {
            // A despesa ja foi salva e o alerta volta no retorno: perder so a notificacao
            // e melhor do que perder o registro do gasto (ADR-0007).
            LOGGER.log(Level.WARNING, "event=alert_dispatch_failed expenseId={0} detector={1} reason={2}",
                alert.expenseId(), alert.detectorName(), ex.getMessage());
        }
    }
}
