package org.example.service.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.example.dto.UserRegisteredEvent;
import org.example.entity.VerificationToken;
import org.example.enums.Role;
import org.example.enums.TokenType;
import org.example.enums.UserStatus;
import org.example.repository.TokenRepository;
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

    @BeforeEach
    void setUp() {
        userRegisteredEventListener = new UserRegisteredEventListener(
                tokenRepository, emailVerificationStrategy, phoneVerificationStrategy);

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
                testEvent.id(),
                "email-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusMinutes(15)
        );
        VerificationToken phoneToken = new VerificationToken(
                testEvent.id(),
                "phone-token",
                TokenType.PHONE_VERIFICATION,
                LocalDateTime.now().plusMinutes(15)
        );
        when(tokenRepository.findByToken(emailToken.token())).thenReturn(Optional.of(emailToken));
        when(tokenRepository.findByToken(phoneToken.token())).thenReturn(Optional.of(phoneToken));

        userRegisteredEventListener.onUserRegistered(testEvent);

        verify(emailVerificationStrategy).sendMessage(testEvent.email(), emailToken);
        verify(phoneVerificationStrategy).sendMessage(testEvent.phoneNumber(), phoneToken);
    }

    @Test
    void shouldHandleExceptionWhenEmailTokenNotFoundAndProceedWithPhone() {
        VerificationToken phoneToken = new VerificationToken(
                testEvent.id(),
                "phone-token",
                TokenType.PHONE_VERIFICATION,
                LocalDateTime.now().plusMinutes(15)
        );
        when(tokenRepository.findByToken("email-token")).thenReturn(Optional.empty());
        when(tokenRepository.findByToken(phoneToken.token())).thenReturn(Optional.of(phoneToken));

        userRegisteredEventListener.onUserRegistered(testEvent);

        verify(emailVerificationStrategy, never()).sendMessage(anyString(), any());
        verify(phoneVerificationStrategy).sendMessage(testEvent.phoneNumber(), phoneToken);
    }

    @Test
    void shouldHandleExceptionWhenPhoneTokenNotFoundAndProceedWithEmail() {
        VerificationToken emailToken = new VerificationToken(
                testEvent.id(),
                "email-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusMinutes(15)
        );
        when(tokenRepository.findByToken(emailToken.token())).thenReturn(Optional.of(emailToken));
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
