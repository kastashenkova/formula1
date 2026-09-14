package org.example.entity;

public record UserEntity(
        Long id,
        String email,
        Role role,
        String password
) {
}
