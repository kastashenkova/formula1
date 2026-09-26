package org.example.service;

import java.util.UUID;
import org.example.command.UpdateUserStatusCommand;
import org.example.dto.UserResponseDto;
import org.example.dto.UserRegistrationRequestDto;

public interface UserService {
    UserResponseDto addUser(UserRegistrationRequestDto requestDto);
    UserResponseDto updateStatus(UUID id, UpdateUserStatusCommand command);
    void confirmByToken(String token);
}
