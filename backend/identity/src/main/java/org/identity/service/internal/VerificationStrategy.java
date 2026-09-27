package org.identity.service.internal;

import java.util.UUID;
import org.identity.entity.VerificationToken;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;

public interface VerificationStrategy {
    TokenType getVerificationTokenType();

    VerificationToken createVerificationToken(UUID userId);

    void sendMessage(String to, VerificationToken token);

    UserStatus getNextStatus(UserStatus currentStatus);
}
