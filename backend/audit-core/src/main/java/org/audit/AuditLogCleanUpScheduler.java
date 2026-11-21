package org.audit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditLogCleanUpScheduler {
    private static final Logger log = LoggerFactory.getLogger(AuditLogCleanUpScheduler.class);
    private final AuditLogRepository auditLogRepository;
    private final int retentionDays;

    public AuditLogCleanUpScheduler(
            AuditLogRepository auditLogRepository,
            @Value("${app.scheduling.audit-cleanup.retention-days:30}") int retentionDays) {
        this.auditLogRepository = auditLogRepository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(
            cron = "${app.scheduling.audit-cleanup.cron:0 0 2 * * *}",
            zone = "Europe/Kyiv"
    )
    @SchedulerLock(
            name = "AuditLogCleanUp",
            lockAtLeastFor = "${app.scheduling.audit-cleanup.lock-at-least:30s}",
            lockAtMostFor = "15m"
    )
    @Transactional
    public void cleanOldAuditLogs() {
        LocalDateTime timestamp = LocalDateTime.now(ZoneId.of("Europe/Kyiv")).minusDays(retentionDays);
        int deletedCount = auditLogRepository.deleteOlderThan(timestamp);

        log.info("ShedLock AuditLogCleanUp: deleted {} audit logs older than {}", deletedCount, timestamp);
    }
}
