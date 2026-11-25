package br.edu.puccampinas.es.expenseguard.domain.model;

public enum RiskLevel {
    BAIXO(1), MEDIO(2), ALTO(3), CRITICO(4);
    private final int severityWeight;
    RiskLevel(int severityWeight) { this.severityWeight = severityWeight; }
    public int getSeverityWeight() { return severityWeight; }
}
