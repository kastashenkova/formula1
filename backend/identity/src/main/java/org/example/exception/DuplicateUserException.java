package org.example.exception;

public class DuplicateUserException extends DomainException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
