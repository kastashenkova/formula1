package org.identity.service.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;
import org.identity.exception.InvalidUserStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationStrategy implements VerificationStrategy {
    private final JavaMailSender mailSender;

    @Value("${email.token.expiry.date}")
    private long expiryHours;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailVerificationStrategy(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
    public void sendMessage(String to, VerificationToken token) {
        String confirmationUrl = frontendUrl + "/auth/confirm-email?token=" + token.getToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Confirm your email to use account in Formula1 App");
        message.setText("Click the link to confirm your email: " + confirmationUrl);
        mailSender.send(message);
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
