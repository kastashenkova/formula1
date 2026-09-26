package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserLoginRequestDto(
        @Email(message = "{validation.email.format}")
        @NotBlank(message = "{validation.email.not-blank}")
        String email,

        @NotBlank(message = "{validation.password.not-blank}")
        @Length(min = 8, max = 35, message = "{validation.password.size}")
        String password
) {
}
