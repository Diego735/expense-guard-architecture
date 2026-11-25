package br.edu.puccampinas.es.expenseguard.infrastructure.resilience;

import br.edu.puccampinas.es.expenseguard.domain.exception.ExternalIntegrationException;
import br.edu.puccampinas.es.expenseguard.domain.model.AnomalyAlert;
import br.edu.puccampinas.es.expenseguard.domain.port.AlertDispatcher;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResilientAlertDispatcher implements AlertDispatcher {
    private final AlertDispatcher delegate;
    private final int maxRetries;
    private final long timeoutMillis;
    private final int failureThreshold;
    private final Duration openStateCooldown;
    private int consecutiveFailures = 0;
    private Instant circuitOpenedAt = null;

    public ResilientAlertDispatcher(AlertDispatcher delegate,
            int maxRetries,
            long timeoutMillis,
            int failureThreshold,
            Duration openStateCooldown) {
        this.delegate = delegate;
        this.maxRetries = maxRetries;
        this.timeoutMillis = timeoutMillis;
        this.failureThreshold = failureThreshold;
        this.openStateCooldown = openStateCooldown;
    }

    @Override
    public void dispatch(AnomalyAlert alert) {
        if (isCircuitOpen()) {
            throw new ExternalIntegrationException("Circuit Breaker ABERTO: envio bloqueado temporariamente.");
        }
        ExternalIntegrationException lastError = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                executeWithTimeout(alert);
                resetCircuit();
                return;
            } catch (RuntimeException ex) {
                lastError = new ExternalIntegrationException("Falha na tentativa " + attempt, ex);
            }
        }
        recordFailure();
        throw lastError;
    }

    private void executeWithTimeout(AnomalyAlert alert) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> delegate.dispatch(alert));
        try {
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new ExternalIntegrationException("Timeout excedido", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExternalIntegrationException("Thread interrompida", ex);
        } catch (ExecutionException ex) {
            throw new ExternalIntegrationException("Erro remoto no adaptador", ex.getCause());
        }
    }

    private boolean isCircuitOpen() {
        if (circuitOpenedAt == null) { return false; }
        if (Instant.now().isAfter(circuitOpenedAt.plus(openStateCooldown))) {
            circuitOpenedAt = null;
            return false;
        }
        return true;
    }

    private void recordFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= failureThreshold) { circuitOpenedAt = Instant.now(); }
    }

    private void resetCircuit() {
        consecutiveFailures = 0;
        circuitOpenedAt = null;
    }
}
