package org.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import org.example.validation.FieldMatch;
import org.example.enums.Role;
import org.example.validation.OnCreate;
import org.example.validation.OnUpdate;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@FieldMatch(groups = {OnCreate.class, OnUpdate.class},
        first = "password",
        second = "repeatPassword",
        message = "{validation.passwords-match.not-match}")
public record UserRegistrationRequestDto(
        @Null(groups = OnCreate.class, message = "{validation.userId.creation.null}")
        @NotNull(groups = OnUpdate.class, message = "{validation.userId.update.not-null}")
        UUID id,

        @Email(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.email.format}")
        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.email.not-blank}")
        String email,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class}, message = "{validation.phone-number.not-blank}")
        @Pattern(
                regexp = "^(\\+380|0)\\d{9}$",
                message = "{validation.phone-number.invalid}"
        )
        @Length(min = 8, max = 13, message = "{validation.phone-number.length}")
        String phoneNumber,

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
