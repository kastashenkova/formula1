package org.example.exception;

public class InvalidVerificationStrategyException extends RuntimeException {
    public InvalidVerificationStrategyException(String message) {
        super(message);
    }
}
