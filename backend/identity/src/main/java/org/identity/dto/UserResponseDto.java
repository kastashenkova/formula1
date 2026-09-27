package org.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.identity.enums.Role;
import org.identity.enums.UserStatus;

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
