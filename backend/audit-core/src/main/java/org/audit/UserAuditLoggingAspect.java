package org.audit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class UserAuditLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(UserAuditLoggingAspect.class);
    private final AuditLogService auditLogService;
    private final AuditUserIdProvider userIdProvider;

    public UserAuditLoggingAspect(AuditLogService auditLogService, AuditUserIdProvider userIdProvider) {
        this.auditLogService = auditLogService;
        this.userIdProvider = userIdProvider;
    }

    @Around("@annotation(auditAction)")
    public Object auditOperation(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        boolean isSuccess = false;
        String errorMessage = null;
        Object result = null;

        UUID currentUserId = null;

        try {
            currentUserId = userIdProvider.getCurrentUserId();
        } catch (Exception e) {
            log.warn("Could not retrieve current user ID for audit logging. Proceeding with null. Reason: {}", e.getMessage());
        }

        try {
            result = joinPoint.proceed();
            isSuccess = true;
            return result;

        } catch (Throwable ex) {
            errorMessage = ex.getMessage();
            throw ex;

        } finally {
            AuditLogEntity auditLog = new AuditLogEntity();
            auditLog.setAction(auditAction.action());
            auditLog.setMethodName(joinPoint.getSignature().toShortString());

            if (auditAction.logParameters()) {
                auditLog.setArguments(Arrays.toString(joinPoint.getArgs()));
            }

            auditLog.setResult(isSuccess && result != null ? result.toString() : null);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setSuccess(isSuccess);
            auditLog.setTimestamp(LocalDateTime.now(ZoneId.of("Europe/Kyiv")));
            auditLog.setUserId(currentUserId);

            try {
                auditLogService.save(auditLog);
            } catch (Exception e) {
                log.error("Failed to save audit log for action: {}", auditAction.action(), e);
            }
        }
    }
}
