package org.example.dto.user.registration;

import org.example.entity.Role;

public record UserRegistrationResponseDto(
        Long id,
        String email,
        Role role
) {
}
