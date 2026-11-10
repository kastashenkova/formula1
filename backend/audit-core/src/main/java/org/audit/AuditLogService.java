package org.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private static final Logger log = LoggerFactory.getLogger(UserAuditLoggingAspect.class);
    private final AuditLogRepository auditRepository;

    public AuditLogService(AuditLogRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void save(AuditLogEntity auditLog) {
        try {
            auditRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log for log: {}", auditLog, e);
        }
    }
}
