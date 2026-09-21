package org.example.repository.user;

import java.util.Optional;
import org.example.entity.UserEntity;

public interface UserRepository {
    UserEntity save(UserEntity user);
    Optional<UserEntity> findById(Long id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    UserEntity updateById(Long id, UserEntity user);
}
