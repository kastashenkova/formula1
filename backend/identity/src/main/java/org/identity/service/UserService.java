package org.identity.service;

import java.util.UUID;
import org.identity.command.UpdateUserStatusCommand;
import org.identity.dto.UserResponseDto;
import org.identity.dto.UserRegistrationRequestDto;

public interface UserService {
    UserResponseDto addUser(UserRegistrationRequestDto requestDto);
    UserResponseDto updateStatus(UUID id, UpdateUserStatusCommand command);
    void confirmByToken(String token);
}
