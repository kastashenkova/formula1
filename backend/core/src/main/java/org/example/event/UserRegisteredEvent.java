package org.example.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.enums.Role;

public record UserRegisteredEvent(
        Long id,
        String email,
        String phoneNumber,
        Role role
) {
}
