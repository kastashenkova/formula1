package org.example.service.user.verification_token;

import org.example.entity.VerificationToken;
import org.example.enums.TokenType;

public interface VerificationTokenStrategy {
    TokenType getVerificationTokenType();

    VerificationToken createVerificationToken(Long userId);

    void sendMessage(String to, VerificationToken token);
}
