package org.identity.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.identity.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRepository extends JpaRepository<VerificationToken, Long> {

    @Query("""
            SELECT t FROM VerificationToken t
            JOIN FETCH t.user
            WHERE t.token = :token
            """)
    Optional<VerificationToken> findByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiryDate < :now")
    int deleteExpiredBefore(@Param("now") LocalDateTime now);

    List<VerificationToken> findAllByUserId(UUID userId);
}
