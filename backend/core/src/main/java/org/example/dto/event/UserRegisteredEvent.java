package org.example.dto.event;

import org.example.enums.Role;
import org.example.enums.UserStatus;

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
