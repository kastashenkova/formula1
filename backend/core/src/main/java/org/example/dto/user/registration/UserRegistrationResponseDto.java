package org.example.dto.user.registration;

import org.example.enums.Role;

public record UserRegistrationResponseDto(
        Long id,
        String email,
        Role role
) {
}
