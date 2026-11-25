package org.identity.dto;

import java.util.UUID;

public record VerificationReminderEvent(
        UUID userId,
        String email,
        String phoneNumber,
        String emailToken,
        String phoneToken
) {
}
