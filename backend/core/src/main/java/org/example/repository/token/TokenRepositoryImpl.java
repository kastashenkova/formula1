package org.example.repository.token;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.example.entity.VerificationToken;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRepositoryImpl implements TokenRepository {

    private final Map<String, VerificationToken> storage = new ConcurrentHashMap<>();

    @Override
    public VerificationToken save(VerificationToken token) {
        storage.put(token.token(), token);
        return token;
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return storage.values().stream()
                .filter(t -> t.token().equals(token))
                .findFirst();
    }

    @Override
    public void delete(VerificationToken token) {
        storage.remove(token.token());
    }
}
