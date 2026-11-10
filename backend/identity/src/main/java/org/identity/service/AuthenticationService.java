package org.identity.service;

import jakarta.validation.Valid;
import org.identity.dto.UserLoginRequestDto;
import org.identity.dto.UserLoginResponseDto;
import org.springframework.validation.annotation.Validated;

@Validated
public interface AuthenticationService {

    @Validated
    UserLoginResponseDto authenticate(@Valid UserLoginRequestDto request);
}
