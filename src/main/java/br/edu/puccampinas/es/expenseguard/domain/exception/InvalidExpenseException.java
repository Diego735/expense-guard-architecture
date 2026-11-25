package br.edu.puccampinas.es.expenseguard.domain.exception;

public class InvalidExpenseException extends RuntimeException {
    public InvalidExpenseException(String message) { super(message); }
}
