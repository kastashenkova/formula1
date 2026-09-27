package org.example.exception;

public class DuplicateBatchException extends DomainException {

    public DuplicateBatchException(String message) {
        super(message);
    }
}
