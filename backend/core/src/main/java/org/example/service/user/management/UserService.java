package org.example.service.user.management;

import org.example.command.UpdateUserStatusCommand;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserRegistrationRequestDto;

public interface UserService {
    UserResponseDto addUser(UserRegistrationRequestDto requestDto);
    UserResponseDto updateStatus(Long id, UpdateUserStatusCommand command);
}
