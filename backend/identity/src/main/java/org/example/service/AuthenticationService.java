package org.example.service;

import org.example.dto.UserLoginRequestDto;
import org.example.dto.UserLoginResponseDto;

public interface AuthenticationService {
    UserLoginResponseDto authenticate(UserLoginRequestDto request);
}
