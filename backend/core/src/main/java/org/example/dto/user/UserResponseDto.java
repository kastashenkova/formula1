package org.example.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.enums.Role;
import org.example.enums.UserStatus;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        @JsonProperty("phone_number")
        String phoneNumber,
        Role role,
        @JsonProperty("user_status")
        UserStatus userStatus
) {

}
