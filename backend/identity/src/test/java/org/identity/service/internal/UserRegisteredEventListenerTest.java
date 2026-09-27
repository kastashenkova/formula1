package org.identity.service.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.identity.dto.UserRegisteredEvent;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.Role;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;
import org.identity.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserRegisteredEventListenerTest {
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private EmailVerificationStrategy emailVerificationStrategy;

    @Mock
    private PhoneVerificationStrategy phoneVerificationStrategy;

    private UserRegisteredEventListener userRegisteredEventListener;

    private UserRegisteredEvent testEvent;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        userRegisteredEventListener = new UserRegisteredEventListener(
                tokenRepository, emailVerificationStrategy, phoneVerificationStrategy);

        testUser = new UserEntity(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.USER,
                "admin123",
                UserStatus.PENDING_VERIFICATION
        );

        testEvent = new UserRegisteredEvent(
                UUID.randomUUID(),
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.USER,
                UserStatus.PENDING_VERIFICATION,
                "email-token",
                "phone-token"
        );
    }

    @Test
    void shouldSendBothVerificationMessagesSuccessfully() {
        VerificationToken emailToken = new VerificationToken(
                1L,
                testUser,
                "email-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusMinutes(15)
        );
        VerificationToken phoneToken = new VerificationToken(
                2L,
                testUser,
                "phone-token",
                TokenType.PHONE_VERIFICATION,
                LocalDateTime.now().plusMinutes(15)
        );
        when(tokenRepository.findByToken(emailToken.getToken())).thenReturn(Optional.of(emailToken));
        when(tokenRepository.findByToken(phoneToken.getToken())).thenReturn(Optional.of(phoneToken));

        userRegisteredEventListener.onUserRegistered(testEvent);

        verify(emailVerificationStrategy).sendMessage(testEvent.email(), emailToken);
        verify(phoneVerificationStrategy).sendMessage(testEvent.phoneNumber(), phoneToken);
    }

    @Test
    void shouldHandleExceptionWhenEmailTokenNotFoundAndProceedWithPhone() {
        VerificationToken phoneToken = new VerificationToken(
                1L,
                testUser,
                "phone-token",
                TokenType.PHONE_VERIFICATION,
                LocalDateTime.now().plusMinutes(15)
        );
        when(tokenRepository.findByToken("email-token")).thenReturn(Optional.empty());
        when(tokenRepository.findByToken(phoneToken.getToken())).thenReturn(Optional.of(phoneToken));

        userRegisteredEventListener.onUserRegistered(testEvent);

        verify(emailVerificationStrategy, never()).sendMessage(anyString(), any());
        verify(phoneVerificationStrategy).sendMessage(testEvent.phoneNumber(), phoneToken);
    }

    @Test
    void shouldHandleExceptionWhenPhoneTokenNotFoundAndProceedWithEmail() {
        VerificationToken emailToken = new VerificationToken(
                1L,
                testUser,
                "email-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusMinutes(15)
        );
        when(tokenRepository.findByToken(emailToken.getToken())).thenReturn(Optional.of(emailToken));
        when(tokenRepository.findByToken("phone-token")).thenReturn(Optional.empty());

        userRegisteredEventListener.onUserRegistered(testEvent);

        verify(emailVerificationStrategy).sendMessage(testEvent.email(), emailToken);
        verify(phoneVerificationStrategy, never()).sendMessage(anyString(), any());
    }

    @Test
    void shouldHandleExceptionWhenPhoneAndEmailTokensNotFound() {
        when(tokenRepository.findByToken("email-token")).thenReturn(Optional.empty());
        when(tokenRepository.findByToken("phone-token")).thenReturn(Optional.empty());

        userRegisteredEventListener.onUserRegistered(testEvent);

        verify(emailVerificationStrategy, never()).sendMessage(anyString(), any());
        verify(phoneVerificationStrategy, never()).sendMessage(anyString(), any());
    }
}
