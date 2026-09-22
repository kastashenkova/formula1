package org.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import org.example.command.UpdateUserStatusCommand;
import org.example.dto.UserRegisteredEvent;
import org.example.dto.UserRegistrationRequestDto;
import org.example.dto.UserResponseDto;
import org.example.entity.UserEntity;
import org.example.entity.VerificationToken;
import org.example.enums.Role;
import org.example.enums.TokenType;
import org.example.enums.UserStatus;
import org.example.exception.DuplicateUserException;
import org.example.exception.InvalidTokenException;
import org.example.exception.InvalidUserStateException;
import org.example.exception.InvalidVerificationStrategyException;
import org.example.repository.TokenRepository;
import org.example.repository.UserRepository;
import org.example.service.internal.UserServiceImpl;
import org.example.service.internal.VerificationStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationStrategy emailStrategy;

    @Mock
    private VerificationStrategy phoneStrategy;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        when(emailStrategy.getVerificationTokenType()).thenReturn(TokenType.EMAIL_VERIFICATION);
        when(phoneStrategy.getVerificationTokenType()).thenReturn(TokenType.PHONE_VERIFICATION);

        userService = new UserServiceImpl(
                passwordEncoder,
                userRepository,
                tokenRepository,
                eventPublisher,
                List.of(emailStrategy, phoneStrategy)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
         UserRegistrationRequestDto testUserRequest = new UserRegistrationRequestDto(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                 "admin123"
        );
        when(userRepository.findByEmail(testUserRequest.email())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(testUserRequest.phoneNumber())).thenReturn(Optional.empty());
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken mockEmailToken = new VerificationToken(
                UUID.randomUUID(),
                "email-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(1));
        VerificationToken mockPhoneToken = new VerificationToken(
                UUID.randomUUID(),
                "phone-token",
                TokenType.PHONE_VERIFICATION,
                LocalDateTime.now().plusHours(1));

        when(emailStrategy.createVerificationToken(any())).thenReturn(mockEmailToken);
        when(phoneStrategy.createVerificationToken(any())).thenReturn(mockPhoneToken);

        UserResponseDto responseDto = userService.addUser(testUserRequest);

        assertNotNull(responseDto);
        assertEquals(testUserRequest.email(), responseDto.email());
        assertEquals(testUserRequest.phoneNumber(), responseDto.phoneNumber());
        assertEquals(testUserRequest.role(), responseDto.role());
        assertEquals(UserStatus.PENDING_VERIFICATION, responseDto.userStatus());

        verify(userRepository).save(any(UserEntity.class));

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("email-token", eventCaptor.getValue().emailVerificationToken());
        assertEquals("phone-token", eventCaptor.getValue().phoneVerificationToken());
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenEmailAlreadyExists() {
        UserRegistrationRequestDto testUserRequest = new UserRegistrationRequestDto(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                "admin123"
        );
        UserEntity existingUser = new UserEntity(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+358408587404",
                Role.ADMIN,
                "admin123",
                UserStatus.ACTIVE
        );
        when(userRepository.findByEmail(testUserRequest.email())).thenReturn(Optional.of(existingUser));

        assertThrows(DuplicateUserException.class, () -> userService.addUser(testUserRequest));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenIdAlreadyExists() {
        UserRegistrationRequestDto testUserRequest = new UserRegistrationRequestDto(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                "admin123"
        );
        UserEntity existingUser = new UserEntity(
                UUID.randomUUID(),
                "k.astashenkova@ukma.edu.ua",
                "+358408587404",
                Role.ADMIN,
                "admin123",
                UserStatus.ACTIVE
        );
        when(userRepository.findById(any())).thenReturn(Optional.of(existingUser));

        assertThrows(DuplicateUserException.class, () -> userService.addUser(testUserRequest));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowInvalidVerificationStrategyExceptionWhenStrategyMissconfigured() {
        UserRegistrationRequestDto testUserRequest = new UserRegistrationRequestDto(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                "admin123"
        );

        when(userRepository.findByEmail(testUserRequest.email())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(testUserRequest.phoneNumber())).thenReturn(Optional.empty());
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        UserServiceImpl serviceWithoutStrategies = new UserServiceImpl(
                passwordEncoder,
                userRepository,
                tokenRepository,
                eventPublisher,
                List.of()
        );

        assertThrows(InvalidVerificationStrategyException.class, ()
                -> serviceWithoutStrategies.addUser(testUserRequest));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenPhoneNumberAlreadyExists() {
        UserRegistrationRequestDto testUserRequest = new UserRegistrationRequestDto(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                "admin123"
        );

        UserEntity existingUser = new UserEntity(
                null,
                "astashenkova.katya@gmail.com",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.ACTIVE
        );

        when(userRepository.findByEmail(testUserRequest.email())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(testUserRequest.phoneNumber()))
                .thenReturn(Optional.of(existingUser));

        assertThrows(DuplicateUserException.class, () -> userService.addUser(testUserRequest));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldUpdateStatusSuccessfullyOnValidTransition() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("exampleUser");
        when(authentication.getName()).thenReturn("d.dzhos@ukma.edu.ua");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserEntity testUser = new UserEntity(
                UUID.randomUUID(),
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.PENDING_VERIFICATION);

        when(userRepository.findById(testUser.id()))
                .thenReturn(Optional.of(testUser));

        when(userRepository.updateById(any(UUID.class), any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        UpdateUserStatusCommand updateUserStatusCommand
                = new UpdateUserStatusCommand(UserStatus.EMAIL_VERIFIED);

        UserResponseDto responseDto = userService.updateStatus(
                testUser.id(), updateUserStatusCommand);

        assertNotNull(responseDto);
        assertEquals(UserStatus.EMAIL_VERIFIED, responseDto.userStatus());
        verify(userRepository).updateById(any(UUID.class), any(UserEntity.class));
    }

    @Test
    void shouldThrowInvalidUserStateExceptionOnIllegalTransition() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("exampleUser");
        when(authentication.getName()).thenReturn("d.dzhos@ukma.edu.ua");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserEntity phoneVerifiedUser = new UserEntity(
                UUID.randomUUID(),
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.PHONE_VERIFIED);

        when(userRepository.findById(phoneVerifiedUser.id()))
                .thenReturn(Optional.of(phoneVerifiedUser));

        UpdateUserStatusCommand updateUserStatusCommand
                = new UpdateUserStatusCommand(UserStatus.PENDING_VERIFICATION);

        assertThrows(InvalidUserStateException.class,
                () -> userService.updateStatus(phoneVerifiedUser.id(), updateUserStatusCommand));

        verify(userRepository, never()).updateById(any(), any());
    }

    @Test
    void shouldThrowAccessDeniedExceptionOnChangingOwnStatus() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("exampleUser");
        when(authentication.getName()).thenReturn("d.dzhos@ukma.edu.ua");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserEntity testUser = new UserEntity(
                UUID.randomUUID(),
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.PHONE_VERIFIED);

        when(userRepository.findByEmail("d.dzhos@ukma.edu.ua")).thenReturn(Optional.of(testUser));

        UpdateUserStatusCommand command = new UpdateUserStatusCommand(UserStatus.DEACTIVATED);

        assertThrows(AccessDeniedException.class,
                () -> userService.updateStatus(testUser.id(), command));

        verify(userRepository, never()).updateById(any(), any());
    }

    @Test
    void shouldConfirmTokenSuccessfully() {
        UserEntity testUser = new UserEntity(
                UUID.randomUUID(),
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.PHONE_VERIFIED);

        VerificationToken verificationToken = new VerificationToken(
                testUser.id(),
                "valid-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken(verificationToken.token())).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById(verificationToken.userId())).thenReturn(Optional.of(testUser));
        when(emailStrategy.getNextStatus(UserStatus.PHONE_VERIFIED)).thenReturn(UserStatus.ACTIVE);

        userService.confirmByToken("valid-token");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).updateById(eq(testUser.id()), userCaptor.capture());

        assertEquals(UserStatus.ACTIVE, userCaptor.getValue().userStatus());
        assertEquals(testUser.id(), userCaptor.getValue().id());
        assertEquals(testUser.email(), userCaptor.getValue().email());
        assertEquals(testUser.phoneNumber(), userCaptor.getValue().phoneNumber());
        assertEquals(testUser.role(), userCaptor.getValue().role());
        assertEquals(testUser.password(), userCaptor.getValue().password());

        verify(tokenRepository).delete(verificationToken);
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenNotFound() {
        VerificationToken verificationToken = new VerificationToken(
                UUID.randomUUID(),
                "invalid-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> userService.confirmByToken(verificationToken.token()));

        verify(userRepository, never()).updateById(any(), any());
        verify(tokenRepository, never()).delete(verificationToken);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        VerificationToken verificationToken = new VerificationToken(
                UUID.randomUUID(),
                "valid-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken(any())).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.confirmByToken(verificationToken.token()));

        verify(userRepository, never()).updateById(any(), any());
        verify(tokenRepository, never()).delete(verificationToken);
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenExpired() {
        VerificationToken verificationToken = new VerificationToken(
                UUID.randomUUID(),
                "invalid-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().minusHours(3));

        when(tokenRepository.findByToken(any())).thenReturn(Optional.of(verificationToken));

        assertThrows(InvalidTokenException.class, () -> userService.confirmByToken(verificationToken.token()));

        verify(userRepository, never()).updateById(any(), any());
        verify(tokenRepository, never()).delete(verificationToken);
    }

    @Test
    void shouldThrowInvalidVerificationStrategyExceptionWhenVerificationStrategyNotFound() {
        UserEntity testUser = new UserEntity(
                UUID.randomUUID(),
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.PHONE_VERIFIED);

        VerificationToken verificationToken = new VerificationToken(
                testUser.id(),
                "valid-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById(testUser.id())).thenReturn(Optional.of(testUser));

        UserServiceImpl serviceWithoutStrategies = new UserServiceImpl(
                passwordEncoder,
                userRepository,
                tokenRepository,
                eventPublisher,
                List.of()
        );

        assertThrows(InvalidVerificationStrategyException.class, () ->
                serviceWithoutStrategies.confirmByToken(verificationToken.token()));

        verify(userRepository, never()).updateById(any(), any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void shouldThrowInvalidUserStateExceptionOnIllegalTransitionDuringConfirmation() {
        UserEntity testUser = new UserEntity(
                UUID.randomUUID(),
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                UserStatus.ACTIVE);

        VerificationToken verificationToken = new VerificationToken(
                testUser.id(),
                "valid-token",
                TokenType.PHONE_VERIFICATION,
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(verificationToken));

        when(userRepository.findById(testUser.id())).thenReturn(Optional.of(testUser));

        when(phoneStrategy.getNextStatus(any())).thenReturn(UserStatus.PHONE_VERIFIED);

        assertThrows(InvalidUserStateException.class,
                () -> userService.confirmByToken(verificationToken.token()));

        verify(userRepository, never()).updateById(any(), any());
        verify(tokenRepository, never()).delete(any());
    }
}
