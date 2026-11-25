package org.identity.service.internal;

import org.identity.dto.UserRegisteredEvent;
import org.identity.dto.VerificationReminderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {
    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);
    private final EmailVerificationStrategy emailVerificationStrategy;
    private final PhoneVerificationStrategy phoneVerificationStrategy;

    UserEventListener(EmailVerificationStrategy emailVerificationStrategy,
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

    @ApplicationModuleListener
    void sendEmailVerificationReminder(VerificationReminderEvent event) {
        if (event.emailToken() == null) {
            return;
        }
        log.info("Sending email verification reminder for user {}", event.userId());
        emailVerificationStrategy.sendMessage(event.email(), event.emailToken());
    }

    @ApplicationModuleListener
    void sendPhoneVerificationReminder(VerificationReminderEvent event) {
        if (event.phoneToken() == null) {
            return;
        }
        log.info("Sending phone verification reminder for user {}", event.userId());
        phoneVerificationStrategy.sendMessage(event.phoneNumber(), event.phoneToken());
    }
}
