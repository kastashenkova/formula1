package org.example.entity;

import org.example.enums.Role;
import org.example.enums.UserStatus;

public record UserEntity(
        Long id,
        String email,
        String phoneNumber,
        Role role,
        String password,
        UserStatus userStatus
) {
}
