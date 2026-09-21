package org.example.service.internal;

import java.util.UUID;
import org.example.entity.VerificationToken;
import org.example.enums.TokenType;
import org.example.enums.UserStatus;

public interface VerificationStrategy {
    TokenType getVerificationTokenType();

    VerificationToken createVerificationToken(UUID userId);

    void sendMessage(String to, VerificationToken token);

    UserStatus getNextStatus(UserStatus currentStatus);
}
