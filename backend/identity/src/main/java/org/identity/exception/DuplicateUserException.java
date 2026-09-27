package org.identity.exception;

public class DuplicateUserException extends DomainException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
