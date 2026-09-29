package org.identity.repository;

import java.util.Optional;
import java.util.UUID;
import org.identity.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRepository extends JpaRepository<VerificationToken, UUID> {

    @Query("""
            SELECT t FROM VerificationToken t
            JOIN FETCH UserEntity u
            WHERE t.token = :token
            """)
    Optional<VerificationToken> findByToken(@Param("token") String token);
}
