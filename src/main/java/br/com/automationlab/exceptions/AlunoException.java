package br.com.automationlab.exceptions;

public class AlunoException extends RuntimeException {
    public AlunoException(String message) {
        super(message);
    }

    public AlunoException(String message, Throwable cause) {
        super(message, cause);
    }
}
