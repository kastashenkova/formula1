package org.example.enums;

public enum BatchStatus {
    UPLOADED,
    PROCESSING,
    FAILED,
    COMPLETED;

    public boolean canTransitionTo(BatchStatus next) {
        return switch (this) {
            case UPLOADED -> next == PROCESSING;
            case PROCESSING -> next == FAILED || next == COMPLETED;
            case FAILED -> next == UPLOADED; // to retry
            case COMPLETED -> false;
        };
    }
}
