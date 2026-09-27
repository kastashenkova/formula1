package org.example.service.internal;

import org.example.entity.VerificationToken;
import org.example.exception.InvalidTokenException;
import org.example.repository.TokenRepository;
import org.example.dto.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventListener {
    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);
    private final TokenRepository tokenRepository;
    private final EmailVerificationStrategy emailVerificationStrategy;
    private final PhoneVerificationStrategy phoneVerificationStrategy;

    UserRegisteredEventListener(TokenRepository tokenRepository,
                                EmailVerificationStrategy emailVerificationStrategy,
                                PhoneVerificationStrategy phoneVerificationStrategy) {
        this.tokenRepository = tokenRepository;
        this.emailVerificationStrategy = emailVerificationStrategy;
        this.phoneVerificationStrategy = phoneVerificationStrategy;
    }

    @ApplicationModuleListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("UserRegisteredEvent received for user {}", event.id());

        try {
            VerificationToken emailToken = tokenRepository
                    .findByToken(event.emailVerificationToken())
                    .orElseThrow(() ->
                            new InvalidTokenException(
                                    "Email verification token not found"
                            )
                    );

            emailVerificationStrategy.sendMessage(
                    event.email(),
                    emailToken
            );
        } catch (Exception e) {
            log.error(
                    "Failed to send email verification for user {}",
                    event.id(),
                    e
            );
        }

        try {
            VerificationToken phoneToken = tokenRepository
                    .findByToken(event.phoneVerificationToken())
                    .orElseThrow(() ->
                            new InvalidTokenException(
                                    "Phone verification token not found"
                            )
                    );

            phoneVerificationStrategy.sendMessage(
                    event.phoneNumber(),
                    phoneToken
            );
        } catch (Exception e) {
            log.error(
                    "Failed to send phone verification for user {}",
                    event.id(),
                    e
            );
        }
    }
}
