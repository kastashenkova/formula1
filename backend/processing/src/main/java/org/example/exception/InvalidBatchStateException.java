package org.example.exception;

public class InvalidBatchStateException extends DomainException {

    public InvalidBatchStateException(String message) {
        super(message);
    }
}
