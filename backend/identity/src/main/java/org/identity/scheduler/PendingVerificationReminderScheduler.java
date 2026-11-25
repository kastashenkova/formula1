package org.identity.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.identity.dto.VerificationReminderEvent;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;
import org.identity.repository.TokenRepository;
import org.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PendingVerificationReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(PendingVerificationReminderScheduler.class);

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final long delayHours;

    public PendingVerificationReminderScheduler(
            UserRepository userRepository,
            TokenRepository tokenRepository,
            ApplicationEventPublisher eventPublisher,
            @Value("${app.scheduling.verification-reminder.delay-hours:12}") long delayHours) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.eventPublisher = eventPublisher;
        this.delayHours = delayHours;
    }

    @Scheduled(
            cron = "${app.scheduling.verification-reminder.cron:0 0 * * * *}",
            zone = "Europe/Kyiv"
    )
    @SchedulerLock(
            name = "PendingVerificationReminder",
            lockAtLeastFor = "${app.scheduling.verification-reminder.lock-at-least:30s}",
            lockAtMostFor = "10m"
    )
    @Transactional
    public void sendVerificationReminders() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Kyiv"));
        LocalDateTime threshold = now.minusHours(delayHours);

        List<UserEntity> pendingUsers = userRepository
                .findByUserStatusAndVerificationReminderSentFalseAndCreatedAtBefore(
                        UserStatus.PENDING_VERIFICATION.name(),
                        threshold
                );

        int remindersQueued = 0;
        for (UserEntity user : pendingUsers) {
            Map<String, String> activeTokens = tokenRepository.findAllByUserId(user.getId()).stream()
                    .filter(token -> token.getExpiryDate().isAfter(now))
                    .collect(Collectors.toMap(
                            VerificationToken::getTokenType,
                            VerificationToken::getToken,
                            (existing, replacement) -> replacement
                    ));

            String emailToken = activeTokens.get(TokenType.EMAIL_VERIFICATION.name());
            String phoneToken = activeTokens.get(TokenType.PHONE_VERIFICATION.name());

            if (emailToken == null && phoneToken == null) {
                continue;
            }

            user.setVerificationReminderSent(true);
            userRepository.save(user);

            eventPublisher.publishEvent(new VerificationReminderEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    emailToken,
                    phoneToken
            ));
            remindersQueued++;
        }

        log.info("ShedLock PendingVerificationReminder: queued {} verification reminders", remindersQueued);
    }
}
