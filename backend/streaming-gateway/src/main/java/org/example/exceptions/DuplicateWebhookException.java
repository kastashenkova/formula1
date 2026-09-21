package org.example.exceptions;

public class DuplicateWebhookException extends DomainException {

    public DuplicateWebhookException(String message) {
        super(message);
    }
}
