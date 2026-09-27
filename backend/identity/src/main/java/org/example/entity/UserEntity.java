package org.example.entity;

import org.example.enums.Role;
import org.example.enums.UserStatus;

import java.util.UUID;

public record UserEntity(
        UUID id,
        String email,
        String phoneNumber,
        Role role,
        String password,
        UserStatus userStatus
) {
}
