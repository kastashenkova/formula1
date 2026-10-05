package org.identity.service.internal;

import formula1.notification.service.EmailSender;
import java.time.LocalDateTime;
import java.util.UUID;
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
public class EmailVerificationStrategy implements VerificationStrategy {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationStrategy.class);

    private final EmailSender emailSender;

    @Value("${formula1.notification.email-token-expiry-hours}")
    private long expiryHours;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailVerificationStrategy(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public TokenType getVerificationTokenType() {
        return TokenType.EMAIL_VERIFICATION;
    }

    @Override
    public VerificationToken createVerificationToken(UserEntity user) {
        String token = UUID.randomUUID().toString();

        LocalDateTime expirationTime = LocalDateTime.now().plusHours(expiryHours);

        return new VerificationToken(
                null,
                user,
                token,
                TokenType.EMAIL_VERIFICATION.toString(),
                expirationTime
        );
    }

    @Override
    public void sendMessage(String to, String token) {
        String confirmationUrl = frontendUrl + "/auth/confirm-email?token=" + token;
        String subject = "Confirm your email to use account in Formula1 App";
        String body = "Click the link to confirm your email: " + confirmationUrl;

        emailSender.sendEmail(to, subject, body);

        log.info("Email confirmation sent to {}", to);
    }

    @Override
    public UserStatus getNextStatus(UserStatus currentStatus) {
        if (currentStatus == UserStatus.PENDING_VERIFICATION) {
            return UserStatus.EMAIL_VERIFIED;
        }
        if (currentStatus == UserStatus.PHONE_VERIFIED) {
            return UserStatus.ACTIVE;
        }

        String errorMessage = String.format("Email cannot be verified from status %s", currentStatus);
        throw new InvalidUserStateException(errorMessage);
    }
}
