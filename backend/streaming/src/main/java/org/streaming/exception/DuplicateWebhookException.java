package org.streaming.exception;

public class DuplicateWebhookException extends DomainException {

    public DuplicateWebhookException(String message) {
        super(message);
    }
}
