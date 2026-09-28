package org.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import org.identity.entity.UserEntity;
import org.identity.enums.Role;
import org.identity.enums.UserStatus;

public record UserResponseDto(
        UUID id,
        String email,
        @JsonProperty("phone_number")
        String phoneNumber,
        Role role,
        @JsonProperty("user_status")
        String userStatus
) {
        public static UserResponseDto fromEntity(UserEntity userEntity) {
                return new UserResponseDto(
                        userEntity.getId(),
                        userEntity.getEmail(),
                        userEntity.getPhoneNumber(),
                        userEntity.getRole(),
                        userEntity.getUserStatus()
                );
        }
}
