package br.edu.puccampinas.es.expenseguard.domain.factory;

import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import br.edu.puccampinas.es.expenseguard.domain.strategy.ZScoreDetectorStrategy;
import java.util.List;

public class StandardProfileDetectorFactory extends DetectorFactory {
    @Override
    public List<AnomalyDetectorStrategy> createDetectors() {
        return List.of(new ZScoreDetectorStrategy(3.0));
    }
}
