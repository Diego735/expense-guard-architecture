package br.edu.puccampinas.es.expenseguard.domain.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import br.edu.puccampinas.es.expenseguard.domain.decorator.AuditedDetectorDecorator;
import br.edu.puccampinas.es.expenseguard.domain.strategy.AnomalyDetectorStrategy;
import java.util.List;
import org.junit.jupiter.api.Test;

class DetectorFactoryTest {
    @Test
    void strictProfileShouldCreateThreeDetectors() {
        List<AnomalyDetectorStrategy> detectors = new StrictProfileDetectorFactory().createDetectors();
        assertEquals(3, detectors.size());
    }

    @Test
    void standardProfileShouldCreateOnlyZScoreDetector() {
        List<AnomalyDetectorStrategy> detectors = new StandardProfileDetectorFactory().createDetectors();
        assertEquals(1, detectors.size());
        assertEquals("ZScoreStatisticalDetector", detectors.get(0).getStrategyName());
    }

    @Test
    void auditedDetectorsShouldWrapEveryDetectorWithDecorator() {
        List<AnomalyDetectorStrategy> detectors = new StrictProfileDetectorFactory().createAuditedDetectors();
        assertEquals(3, detectors.size());
        detectors.forEach(detector -> assertInstanceOf(AuditedDetectorDecorator.class, detector));
    }
}
