package org.identity.service.internal;

import org.identity.dto.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventListener {
    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);
    private final EmailVerificationStrategy emailVerificationStrategy;
    private final PhoneVerificationStrategy phoneVerificationStrategy;

    UserRegisteredEventListener(EmailVerificationStrategy emailVerificationStrategy,
                                PhoneVerificationStrategy phoneVerificationStrategy) {
        this.emailVerificationStrategy = emailVerificationStrategy;
        this.phoneVerificationStrategy = phoneVerificationStrategy;
    }

    @ApplicationModuleListener
    void sendEmailVerification(UserRegisteredEvent event) {
        log.info("Sending email verification for user {}", event.id());
        emailVerificationStrategy.sendMessage(event.email(), event.emailVerificationToken());
    }

    @ApplicationModuleListener
    void sendPhoneVerification(UserRegisteredEvent event) {
        log.info("Sending phone verification for user {}", event.id());
        phoneVerificationStrategy.sendMessage(event.phoneNumber(), event.phoneVerificationToken());
    }
}
