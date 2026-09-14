package org.example.dto.user.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.example.validation.FieldMatch;
import org.example.entity.Role;
import org.example.validation.OnCreate;
import org.example.validation.OnUpdate;
import org.hibernate.validator.constraints.Length;

@FieldMatch(groups = {OnCreate.class, OnUpdate.class},
        first = "password",
        second = "repeatPassword",
        message = "{validation.passwords-match.not-match}")
public record UserRegistrationRequestDto(
        @Null(groups = OnCreate.class, message = "{validation.userId.creation.null}")
        @NotNull(groups = OnUpdate.class, message = "{validation.userId.update.not-null}")
        Long id,

        @Email(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.email.format}")
        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.email.not-blank}")
        String email,

        @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.role.not-null}")
        Role role,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.password.not-blank}")
        @Length(groups = {OnCreate.class, OnUpdate.class}, min = 8, max = 35, message = "{validation.password.size}")
        String password,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.password.not-blank}")
        @Length(groups = {OnCreate.class, OnUpdate.class}, min = 8, max = 35, message = "{validation.password.size}")
        String repeatPassword
) {
}
