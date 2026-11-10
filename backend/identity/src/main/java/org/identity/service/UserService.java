package org.identity.service;

import jakarta.validation.Valid;
import java.util.UUID;
import org.identity.command.UpdateUserStatusCommand;
import org.identity.dto.UserResponseDto;
import org.identity.dto.UserRegistrationRequestDto;
import org.identity.validation.OnCreate;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UserService {

    @Validated(OnCreate.class)
    UserResponseDto addUser(@Valid UserRegistrationRequestDto requestDto);

    UserResponseDto updateStatus(UUID id, UpdateUserStatusCommand command);

    void confirmByToken(String token);
}
