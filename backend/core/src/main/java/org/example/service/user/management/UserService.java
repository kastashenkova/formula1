package org.example.service.user.management;

import java.util.UUID;
import org.example.command.UpdateUserStatusCommand;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserRegistrationRequestDto;

public interface UserService {
    UserResponseDto addUser(UserRegistrationRequestDto requestDto);
    UserResponseDto updateStatus(UUID id, UpdateUserStatusCommand command);
    void confirmByToken(String token);
}
