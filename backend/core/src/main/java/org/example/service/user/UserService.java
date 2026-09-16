package org.example.service.user;

import org.example.dto.user.registration.UserRegistrationRequestDto;
import org.example.dto.user.registration.UserRegistrationResponseDto;

public interface UserService {

    UserRegistrationResponseDto addUser(UserRegistrationRequestDto requestDto);
}
