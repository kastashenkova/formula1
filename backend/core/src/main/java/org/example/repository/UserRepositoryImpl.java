package org.example.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.example.entity.UserEntity;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, UserEntity> storage = new ConcurrentHashMap<>();

    @Override
    public UserEntity save(UserEntity user) {
        storage.put(user.id(), user);
        return user;
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
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
}
