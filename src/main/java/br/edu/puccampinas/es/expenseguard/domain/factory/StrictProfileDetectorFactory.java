package br.edu.puccampinas.es.expenseguard.domain.factory;

import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import br.edu.puccampinas.es.expenseguard.domain.strategy.IqrOutlierDetectorStrategy;
import br.edu.puccampinas.es.expenseguard.domain.strategy.VelocitySpikeDetectorStrategy;
import br.edu.puccampinas.es.expenseguard.domain.strategy.ZScoreDetectorStrategy;
import java.time.Duration;
import java.util.List;

public class StrictProfileDetectorFactory extends DetectorFactory {
    @Override
    public List<AnomalyDetectorStrategy> createDetectors() {
        return List.of(new ZScoreDetectorStrategy(2.0),
            new IqrOutlierDetectorStrategy(),
            new VelocitySpikeDetectorStrategy(3,
            Duration.ofMinutes(30)));
    }
}
