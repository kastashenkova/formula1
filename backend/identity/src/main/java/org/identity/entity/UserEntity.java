package org.identity.entity;

import org.identity.enums.Role;
import org.identity.enums.UserStatus;

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
