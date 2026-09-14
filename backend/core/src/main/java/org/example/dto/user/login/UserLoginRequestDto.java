package org.example.dto.user.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.validation.OnCreate;
import org.example.validation.OnUpdate;
import org.hibernate.validator.constraints.Length;

public record UserLoginRequestDto(
        @Email(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.email.format}")
        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.email.not-blank}")
        String email,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.password.not-blank}")
        @Length(groups = {OnCreate.class, OnUpdate.class}, min = 8, max = 35, message = "{validation.password.size}")
        String password
) {
}
