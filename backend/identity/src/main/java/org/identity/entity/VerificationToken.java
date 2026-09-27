package org.identity.entity;

import org.identity.enums.TokenType;
import java.time.LocalDateTime;
import java.util.UUID;

public record VerificationToken(
        UUID userId,
        String token,
        TokenType tokenType,
        LocalDateTime expiryDate
) {
}
