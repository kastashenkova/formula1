package org.identity.dto;

import org.identity.enums.Role;
import org.identity.enums.UserStatus;

import java.util.UUID;

public record UserRegisteredEvent(
        UUID id,
        String email,
        String phoneNumber,
        Role role,
        UserStatus userStatus,
        String emailVerificationToken,
        String phoneVerificationToken
) {
}
