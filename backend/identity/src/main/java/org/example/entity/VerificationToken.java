package org.example.entity;

import org.example.enums.TokenType;
import java.time.LocalDateTime;
import java.util.UUID;

public record VerificationToken(
        UUID userId,
        String token,
        TokenType tokenType,
        LocalDateTime expiryDate
) {
}
