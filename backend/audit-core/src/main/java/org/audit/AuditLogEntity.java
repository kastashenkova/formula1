package org.audit;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "audit_log")
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String action;
    @Column(name = "user_id")
    private UUID userId;
    @Column(nullable = false, name = "method_name")
    private String methodName;
    @Column(length = 2048)
    private String arguments;
    @Column(length = 2048)
    private String result;
    @Column(name = "error_message", length = 2048)
    private String errorMessage;
    @Column(nullable = false)
    private boolean success;
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
