package org.example.entity;

import org.example.enums.Role;

public record UserEntity(
        Long id,
        String email,
        Role role,
        String password
) {
}
