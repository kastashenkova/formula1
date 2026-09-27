package org.example.repository.internal;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.example.entity.UserEntity;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final static Map<UUID, UserEntity> storage = new ConcurrentHashMap<>();

    @Override
    public UserEntity save(UserEntity user) {
        storage.put(user.id(), user);
        return user;
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return storage.values().stream()
                .filter(c -> c.email().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Optional<UserEntity> findByPhoneNumber(String phoneNumber) {
        return storage.values().stream()
                .filter(c -> c.phoneNumber().equalsIgnoreCase(phoneNumber))
                .findFirst();
    }

    @Override
    public UserEntity updateById(UUID id, UserEntity request) {
        if (storage.containsKey(id)) {
            storage.put(id, request);
            return request;
        }
        return null;
    }
}
