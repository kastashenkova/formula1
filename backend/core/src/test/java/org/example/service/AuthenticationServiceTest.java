package org.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.example.dto.UserLoginRequestDto;
import org.example.dto.UserLoginResponseDto;
import org.example.entity.UserEntity;
import org.example.enums.Role;
import org.example.enums.UserStatus;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.example.service.internal.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationServiceImpl(jwtUtil, authenticationManager, userRepository);
    }

    @Test
    void shouldReturnToken() {
        UserLoginRequestDto userLoginRequestDto = new UserLoginRequestDto(
                "k.astashenkova@ukma.edu.ua",
                "admin123"
        );

        UserEntity existingUser = new UserEntity(
                UUID.randomUUID(),
                userLoginRequestDto.email(),
                "+380980137037",
                Role.ADMIN,
                "hashedPass",
                UserStatus.ACTIVE);

        when(userRepository.findByEmail(userLoginRequestDto.email())).thenReturn(Optional.of(existingUser));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(userLoginRequestDto.email());
        String mockToken = "mockToken";
        when(jwtUtil.generateToken(userLoginRequestDto.email()))
                .thenReturn(mockToken);

        UserLoginResponseDto actual = service.authenticate(userLoginRequestDto);

        assertNotNull(actual);
        assertEquals(mockToken, actual.token());
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1))
                .generateToken(userLoginRequestDto.email());
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenEmailDoesNotExist() {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "k.astashenkova@ukma.edu.ua",
                "admin123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> service.authenticate(request));

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenPasswordIsInvalid() {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "k.astashenkova@ukma.edu.ua",
                "admin123");

        UserEntity activeUser = new UserEntity(
                UUID.randomUUID(),
                request.email(),
                "+380980137037",
                Role.USER,
                "user123",
                UserStatus.ACTIVE);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(activeUser));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> service.authenticate(request));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void shouldThrowDisabledExceptionWhenUserIsDeactivated() {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "k.astashenkova@ukma.edu.ua",
                "admin123");

        UserEntity deactivatedUser = new UserEntity(
                UUID.randomUUID(), request.email(),
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.DEACTIVATED);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(deactivatedUser));

        assertThrows(DisabledException.class, () -> service.authenticate(request));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldThrowDisabledExceptionWhenUserNotVerifiedPhoneNumber() {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "k.astashenkova@ukma.edu.ua",
                "admin123");

        UserEntity notVerifiedUser = new UserEntity(
                UUID.randomUUID(), request.email(),
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.EMAIL_VERIFIED);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(notVerifiedUser));

        assertThrows(DisabledException.class, () -> service.authenticate(request));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldThrowDisabledExceptionWhenUserNotVerifiedEmail() {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "k.astashenkova@ukma.edu.ua",
                "admin123");

        UserEntity notVerifiedUser = new UserEntity(
                UUID.randomUUID(), request.email(),
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.PHONE_VERIFIED);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(notVerifiedUser));

        assertThrows(DisabledException.class, () -> service.authenticate(request));

        verify(authenticationManager, never()).authenticate(any());
    }
}
