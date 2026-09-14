package org.example.service.user;

import org.example.dto.user.registration.UserRegistrationRequestDto;
import org.example.dto.user.registration.UserRegistrationResponseDto;

public interface UsersService {

    UserRegistrationResponseDto addUser(UserRegistrationRequestDto requestDto);
}
