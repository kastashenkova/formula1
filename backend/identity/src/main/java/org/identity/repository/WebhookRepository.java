package org.identity.repository;

import java.util.Optional;
import org.identity.entity.WebhookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookRepository extends JpaRepository<WebhookEntity, Long> {

    @Query("""
            SELECT w FROM WebhookEntity w
            JOIN FETCH w.user
            WHERE w.id = :id
            """)
    Optional<WebhookEntity> findByIdWithDetails(@Param("id") Long id);
}
