package br.edu.puccampinas.es.expenseguard.infrastructure.notification;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.port.AlertDispatcher;

public class ConsoleFirebaseAlertDispatcher implements AlertDispatcher {
    @Override
    public void dispatch(AnomalyAlert alert) {
        System.out.printf("[ALERTA %s] Usuario=%s | Detector=%s | %s%n",
            alert.riskLevel(),
            alert.userId(),
            alert.detectorName(),
            alert.explanation());
    }
}
