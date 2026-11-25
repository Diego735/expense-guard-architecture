package br.edu.puccampinas.es.expenseguard.domain.port;

import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;

public interface AlertDispatcher {
    void dispatch(AnomalyAlert alert);
}
