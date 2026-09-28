package org.streaming.exception;

public class DuplicateRaceException extends DomainException {
    public DuplicateRaceException(String message) {
        super(message);
    }
}
