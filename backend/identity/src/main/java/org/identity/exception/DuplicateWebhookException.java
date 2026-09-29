package org.identity.exception;

public class DuplicateWebhookException extends DomainException {

    public DuplicateWebhookException(String message) {
        super(message);
    }
}
