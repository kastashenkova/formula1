package org.identity.repository;

import java.util.Optional;
import org.identity.entity.VerificationToken;

public interface TokenRepository {
    VerificationToken save(VerificationToken token);
    Optional<VerificationToken> findByToken(String token);
    void delete(VerificationToken token);
}
