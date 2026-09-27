package org.example.exception;

public class InvalidUserStateException extends DomainException {

    public InvalidUserStateException(String message) {
        super(message);
    }
}
