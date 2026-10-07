package org.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import org.identity.command.UpdateUserStatusCommand;
import org.identity.dto.UserRegisteredEvent;
import org.identity.dto.UserRegistrationRequestDto;
import org.identity.dto.UserResponseDto;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.Role;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;
import org.identity.exception.DuplicateUserException;
import org.identity.exception.InvalidTokenException;
import org.identity.exception.InvalidUserStateException;
import org.identity.exception.InvalidVerificationStrategyException;
import org.identity.repository.TokenRepository;
import org.identity.repository.UserRepository;
import org.identity.service.internal.UserServiceImpl;
import org.identity.service.internal.VerificationStrategy;
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
                Role.ADMIN.toString(),
                "admin123",
                 "admin123"
        );

         UserEntity testEntity = new UserEntity(
                 testUserRequest.email(),
                 testUserRequest.phoneNumber(),
                 testUserRequest.role(),
                 testUserRequest.password(),
                 UserStatus.PENDING_VERIFICATION.toString()
         );

        when(userRepository.existsByEmail(testUserRequest.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(testUserRequest.phoneNumber())).thenReturn(false);
        when(userRepository.findById((UUID) any())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken mockEmailToken = new VerificationToken(
                1L,
                testEntity,
                "email-token",
                TokenType.EMAIL_VERIFICATION.toString(),
                LocalDateTime.now().plusHours(1));
        VerificationToken mockPhoneToken = new VerificationToken(
                1L,
                testEntity,
                "phone-token",
                TokenType.PHONE_VERIFICATION.toString(),
                LocalDateTime.now().plusHours(1));

        when(emailStrategy.createVerificationToken(any())).thenReturn(mockEmailToken);
        when(phoneStrategy.createVerificationToken(any())).thenReturn(mockPhoneToken);

        UserResponseDto responseDto = userService.addUser(testUserRequest);

        assertNotNull(responseDto);
        assertEquals(testUserRequest.email(), responseDto.email());
        assertEquals(testUserRequest.phoneNumber(), responseDto.phoneNumber());
        assertEquals(testUserRequest.role(), responseDto.role());
        assertEquals(UserStatus.PENDING_VERIFICATION.toString(), responseDto.userStatus());

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
                Role.ADMIN.toString(),
                "admin123",
                "admin123"
        );

        when(userRepository.existsByEmail(testUserRequest.email())).thenReturn(true);

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
                Role.ADMIN.toString(),
                "admin123",
                "admin123"
        );
        UserEntity existingUser = new UserEntity(
                "k.astashenkova@ukma.edu.ua",
                "+358408587404",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.ACTIVE.toString()
        );
        when(userRepository.findById((UUID) any())).thenReturn(Optional.of(existingUser));

        assertThrows(DuplicateUserException.class, () -> userService.addUser(testUserRequest));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowInvalidVerificationStrategyExceptionWhenStrategyMisconfigured() {
        UserRegistrationRequestDto testUserRequest = new UserRegistrationRequestDto(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                "admin123"
        );

        when(userRepository.existsByEmail(testUserRequest.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(testUserRequest.phoneNumber())).thenReturn(false);
        when(userRepository.findById((UUID) any())).thenReturn(Optional.empty());

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
                Role.ADMIN.toString(),
                "admin123",
                "admin123"
        );

        when(userRepository.existsByEmail(testUserRequest.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(testUserRequest.phoneNumber()))
                .thenReturn(true);

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
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PENDING_VERIFICATION.toString());

        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserStatusCommand updateUserStatusCommand
                = new UpdateUserStatusCommand(UserStatus.EMAIL_VERIFIED);

        UserResponseDto responseDto = userService.updateStatus(
                testUser.getId(), updateUserStatusCommand);

        assertNotNull(responseDto);
        assertEquals(UserStatus.EMAIL_VERIFIED.toString(), responseDto.userStatus());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowInvalidUserStateExceptionOnIllegalTransition() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("exampleUser");
        when(authentication.getName()).thenReturn("d.dzhos@ukma.edu.ua");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserEntity phoneVerifiedUser = new UserEntity(
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        when(userRepository.findById(phoneVerifiedUser.getId()))
                .thenReturn(Optional.of(phoneVerifiedUser));

        UpdateUserStatusCommand updateUserStatusCommand
                = new UpdateUserStatusCommand(UserStatus.PENDING_VERIFICATION);

        assertThrows(InvalidUserStateException.class,
                () -> userService.updateStatus(phoneVerifiedUser.getId(), updateUserStatusCommand));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowAccessDeniedExceptionOnChangingOwnStatus() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("exampleUser");
        when(authentication.getName()).thenReturn("d.dzhos@ukma.edu.ua");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        UUID userId = UUID.randomUUID();
        testUser.setId(userId);

        when(userRepository.findByEmail("d.dzhos@ukma.edu.ua")).thenReturn(Optional.of(testUser));

        UpdateUserStatusCommand command = new UpdateUserStatusCommand(UserStatus.DEACTIVATED);

        assertThrows(AccessDeniedException.class,
                () -> userService.updateStatus(testUser.getId(), command));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldConfirmTokenSuccessfully() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        VerificationToken verificationToken = new VerificationToken(
                1L,
                testUser,
                "valid-token",
                TokenType.EMAIL_VERIFICATION.toString(),
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken(verificationToken.getToken())).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById(verificationToken.getUser().getId())).thenReturn(Optional.of(testUser));
        when(emailStrategy.getNextStatus(UserStatus.PHONE_VERIFIED)).thenReturn(UserStatus.ACTIVE);

        userService.confirmByToken("valid-token");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals(UserStatus.ACTIVE.toString(), userCaptor.getValue().getUserStatus());
        assertEquals(testUser.getId(), userCaptor.getValue().getId());
        assertEquals(testUser.getEmail(), userCaptor.getValue().getEmail());
        assertEquals(testUser.getPhoneNumber(), userCaptor.getValue().getPhoneNumber());
        assertEquals(testUser.getRole(), userCaptor.getValue().getRole());
        assertEquals(testUser.getPassword(), userCaptor.getValue().getPassword());

        verify(tokenRepository).delete(verificationToken);
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenNotFound() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        VerificationToken verificationToken = new VerificationToken(
                1L,
                testUser,
                "invalid-token",
                TokenType.EMAIL_VERIFICATION.toString(),
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> userService.confirmByToken(
                verificationToken.getToken()));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(verificationToken);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        VerificationToken verificationToken = new VerificationToken(
                1L,
                testUser,
                "valid-token",
                TokenType.EMAIL_VERIFICATION.toString(),
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken(any())).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById((UUID) any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService
                .confirmByToken(verificationToken.getToken()));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(verificationToken);
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenExpired() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        VerificationToken verificationToken = new VerificationToken(
                1L,
                testUser,
                "invalid-token",
                TokenType.EMAIL_VERIFICATION.toString(),
                LocalDateTime.now().minusHours(3));

        when(tokenRepository.findByToken(any())).thenReturn(Optional.of(verificationToken));

        assertThrows(InvalidTokenException.class, () -> userService.confirmByToken(
                verificationToken.getToken()));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(verificationToken);
    }

    @Test
    void shouldThrowInvalidVerificationStrategyExceptionWhenVerificationStrategyNotFound() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        VerificationToken verificationToken = new VerificationToken(
                1L,
                testUser,
                "valid-token",
                TokenType.EMAIL_VERIFICATION.toString(),
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        UserServiceImpl serviceWithoutStrategies = new UserServiceImpl(
                passwordEncoder,
                userRepository,
                tokenRepository,
                eventPublisher,
                List.of()
        );

        assertThrows(InvalidVerificationStrategyException.class, () ->
                serviceWithoutStrategies.confirmByToken(verificationToken.getToken()));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void shouldThrowInvalidUserStateExceptionOnIllegalTransitionDuringConfirmation() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.ACTIVE.toString());

        VerificationToken verificationToken = new VerificationToken(
                1L,
                testUser,
                "valid-token",
                TokenType.PHONE_VERIFICATION.toString(),
                LocalDateTime.now().plusHours(3));

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(verificationToken));

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        when(phoneStrategy.getNextStatus(any())).thenReturn(UserStatus.PHONE_VERIFIED);

        assertThrows(InvalidUserStateException.class,
                () -> userService.confirmByToken(verificationToken.getToken()));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }
}
