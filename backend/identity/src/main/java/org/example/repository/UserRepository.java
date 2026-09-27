package org.example.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.entity.UserEntity;

public interface UserRepository {
    UserEntity save(UserEntity user);
    Optional<UserEntity> findById(UUID id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    UserEntity updateById(UUID id, UserEntity user);
}
