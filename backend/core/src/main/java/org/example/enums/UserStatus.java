package org.example.enums;

public enum UserStatus {
    PENDING_VERIFICATION,
    EMAIL_VERIFIED,
    PHONE_VERIFIED,
    ACTIVE,
    DEACTIVATED;

    public boolean canTransitionTo(UserStatus next) {
        return switch (this) {
            case PENDING_VERIFICATION ->
                    next == EMAIL_VERIFIED || next == PHONE_VERIFIED || next == DEACTIVATED;

            case EMAIL_VERIFIED, PHONE_VERIFIED ->
                    next == ACTIVE || next == DEACTIVATED;

            case ACTIVE -> next == DEACTIVATED;

            case DEACTIVATED -> false;
        };
    }
}
