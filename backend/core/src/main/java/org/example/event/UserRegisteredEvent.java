package org.example.event;

import org.example.enums.Role;
import org.example.enums.UserStatus;

public record UserRegisteredEvent(
        Long id,
        String email,
        String phoneNumber,
        Role role,
        UserStatus userStatus
) {
}
