package br.edu.puccampinas.es.expenseguard.domain.factory;

import br.edu.puccampinas.es.expenseguard.domain.decorator.AuditedDetectorDecorator;
import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import java.util.List;

public abstract class DetectorFactory {
    /** Factory Method: cada perfil de risco decide quais detectores usar e com qual calibracao. */
    public abstract List<AnomalyDetectorStrategy> createDetectors();

    public List<AnomalyDetectorStrategy> createAuditedDetectors() {
        return createDetectors().stream()
                .map(detector -> (AnomalyDetectorStrategy) new AuditedDetectorDecorator(detector))
                .toList();
    }
}
