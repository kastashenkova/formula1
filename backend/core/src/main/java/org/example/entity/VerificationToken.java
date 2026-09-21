package org.example.entity;

import org.example.enums.TokenType;
import java.time.LocalDateTime;

public record VerificationToken(
        Long userId,
        String token,
        TokenType tokenType,
        LocalDateTime expiryDate
) {
}
