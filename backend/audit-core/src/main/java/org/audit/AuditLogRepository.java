package org.audit;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    @Modifying
    @Query("DELETE FROM AuditLogEntity a WHERE a.timestamp < :timestamp")
    int deleteOlderThan(@Param("timestamp") LocalDateTime timestamp);
}
