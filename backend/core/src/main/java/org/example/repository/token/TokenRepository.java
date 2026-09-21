package org.example.repository.token;

import org.example.entity.VerificationToken;

public interface TokenRepository {
    VerificationToken save(VerificationToken token);
}
