package org.example.config;

import org.audit.AuditLogEntity;
import org.audit.AuditLogRepository;
import org.example.BackendApplication;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.entity.WebhookEntity;
import org.identity.repository.TokenRepository;
import org.identity.repository.UserRepository;
import org.identity.repository.WebhookRepository;
import org.processing.entity.BatchEntity;
import org.processing.entity.WorkItemEntity;
import org.processing.repository.BatchRepository;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.streaming.entity.DriverEntity;
import org.streaming.entity.RaceEntity;
import org.streaming.entity.TelemetryPointEntity;
import org.streaming.repository.DriverRepository;
import org.streaming.repository.RaceRepository;
import org.streaming.repository.TelemetryPointRepository;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = {
                BackendApplication.class,
                RaceRepository.class,
                UserRepository.class,
                DriverRepository.class,
                TelemetryPointRepository.class,
                BatchRepository.class,
                WebhookRepository.class,
                AuditLogRepository.class,
                TokenRepository.class
        })
@EntityScan(basePackageClasses = {
        BackendApplication.class,
        RaceEntity.class,
        DriverEntity.class,
        VerificationToken.class,
        UserEntity.class,
        TelemetryPointEntity.class,
        WebhookEntity.class,
        BatchEntity.class,
        WorkItemEntity.class,
        AuditLogEntity.class
})
public class JpaConfig {
}
