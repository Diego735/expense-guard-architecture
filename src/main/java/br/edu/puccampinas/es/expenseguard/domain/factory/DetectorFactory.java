package br.edu.puccampinas.es.expenseguard.domain.factory;

import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import java.util.List;

public abstract class DetectorFactory {
    public abstract List<AnomalyDetectorStrategy> createDetectors();
}
