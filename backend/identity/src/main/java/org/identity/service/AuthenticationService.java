package org.identity.service;

import org.identity.dto.UserLoginRequestDto;
import org.identity.dto.UserLoginResponseDto;

public interface AuthenticationService {
    UserLoginResponseDto authenticate(UserLoginRequestDto request);
}
