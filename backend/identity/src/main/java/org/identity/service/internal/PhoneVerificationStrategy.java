package org.identity.service.internal;

import java.time.LocalDateTime;
import java.util.UUID;
import formula1.notification.service.WhatsAppSender;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;
import org.identity.exception.InvalidUserStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhoneVerificationStrategy implements VerificationStrategy {
    private static final Logger log = LoggerFactory.getLogger(PhoneVerificationStrategy.class);

    @Value("${formula1.notification.phone-token-expiry-hours}")
    private long expiryHours;

    private final WhatsAppSender whatsAppSender;

    public PhoneVerificationStrategy(WhatsAppSender whatsAppSender) {
        this.whatsAppSender = whatsAppSender;
    }

    @Override
    public TokenType getVerificationTokenType() {
        return TokenType.PHONE_VERIFICATION;
    }

    @Override
    public VerificationToken createVerificationToken(UserEntity user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(expiryHours);

        return new VerificationToken(
                null,
                user,
                token,
                TokenType.PHONE_VERIFICATION.toString(),
                expirationTime
        );
    }

    @Override
    public void sendMessage(String to, String token) {
        whatsAppSender.sendMessage(to, "verificaion_link", token);

        log.info("Phone confirmation sent to {}", to);
    }

    @Override
    public UserStatus getNextStatus(UserStatus currentStatus) {
        if (currentStatus == UserStatus.PENDING_VERIFICATION) {
            return UserStatus.PHONE_VERIFIED;
        }
        if (currentStatus == UserStatus.EMAIL_VERIFIED) {
            return UserStatus.ACTIVE;
        }

        throw new InvalidUserStateException("Phone cannot be verified from status: " + currentStatus);
    }
}
