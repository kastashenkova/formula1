package org.identity.service.internal;

import org.identity.dto.UserLoginRequestDto;
import org.identity.dto.UserLoginResponseDto;
import org.identity.entity.UserEntity;
import org.identity.enums.UserStatus;
import org.identity.repository.UserRepository;
import org.identity.security.JwtUtil;
import org.identity.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    public AuthenticationServiceImpl(JwtUtil jwtUtil,
                                     AuthenticationManager authenticationManager,
                                     UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @Override
    public UserLoginResponseDto authenticate(UserLoginRequestDto request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.userStatus() == UserStatus.DEACTIVATED) {
            throw new DisabledException("User is deactivated and cannot log in");
        }

        if (user.userStatus() == UserStatus.PENDING_VERIFICATION
                || user.userStatus() == UserStatus.EMAIL_VERIFIED
                || user.userStatus() == UserStatus.PHONE_VERIFIED) {
            throw new DisabledException("User is not active. Please, verify your email and phone number");
        }

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String token = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(token);
    }
}
