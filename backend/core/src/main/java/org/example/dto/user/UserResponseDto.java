package org.example.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.enums.Role;
import org.example.enums.UserStatus;

public record UserResponseDto(
        Long id,
        String email,
        @JsonProperty("phone_number")
        String phoneNumber,
        Role role,
        @JsonProperty("user_status")
        UserStatus userStatus
) {

}
