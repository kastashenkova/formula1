package org.example.service.authentication;

import org.example.dto.user.login.UserLoginRequestDto;
import org.example.dto.user.login.UserLoginResponseDto;

public interface AuthenticationService {
    UserLoginResponseDto authenticate(UserLoginRequestDto request);
}
