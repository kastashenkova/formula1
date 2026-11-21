package org.identity.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.identity.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExpiredTokensCleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(ExpiredTokensCleanupScheduler.class);
    private final TokenRepository tokenRepository;

    public ExpiredTokensCleanupScheduler(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(
            cron = "${app.scheduling.expired-tokens-cleanup.cron:0 0 * * * *}",
            zone = "Europe/Kyiv"
    )
    @SchedulerLock(
            name = "ExpiredTokensCleanUp",
            lockAtLeastFor = "${app.scheduling.expired-tokens-cleanup.lock-at-least:30s}",
            lockAtMostFor = "5m"
    )
    @Transactional
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Kyiv"));
        int deletedCount = tokenRepository.deleteExpiredBefore(now);

        log.info("ShedLock ExpiredTokensCleanUp: deleted {} expired verification tokens", deletedCount);
    }
}
