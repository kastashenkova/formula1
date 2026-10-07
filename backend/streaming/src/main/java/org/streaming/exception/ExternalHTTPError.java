package org.streaming.exception;

public class ExternalHTTPError extends DomainException {
    public ExternalHTTPError(String message) {
        super(message);
    }
}
