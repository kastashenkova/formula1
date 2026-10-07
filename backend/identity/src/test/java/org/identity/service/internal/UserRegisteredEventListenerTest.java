package org.identity.service.internal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.UUID;
import org.identity.dto.UserRegisteredEvent;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.Role;
import org.identity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRegisteredEventListenerTest {
    @Mock
    private EmailVerificationStrategy emailVerificationStrategy;
    @Mock
    private PhoneVerificationStrategy phoneVerificationStrategy;

    private UserRegisteredEventListener listener;
    private UserRegisteredEvent testEvent;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        listener = new UserRegisteredEventListener(
                emailVerificationStrategy, phoneVerificationStrategy);

        testUser = new UserEntity(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.USER.toString(),
                "admin123",
                UserStatus.PENDING_VERIFICATION.toString()
        );

        testEvent = new UserRegisteredEvent(
                UUID.randomUUID(),
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.USER.toString(),
                UserStatus.PENDING_VERIFICATION.toString(),
                "email-token",
                "phone-token"
        );
    }

    @Test
    void shouldSendEmailVerificationWithTokenFromEvent() {
        listener.sendEmailVerification(testEvent);

        verify(emailVerificationStrategy).sendMessage("k.astashenkova@ukma.edu.ua", "email-token");
        verifyNoInteractions(phoneVerificationStrategy);
    }

    @Test
    void shouldSendPhoneVerificationWithTokenFromEvent() {
        listener.sendPhoneVerification(testEvent);

        verify(phoneVerificationStrategy).sendMessage("+380980137037", "phone-token");
        verifyNoInteractions(emailVerificationStrategy);
    }

    @Test
    void shouldPropagateEmailSendFailureSoPublicationStaysIncomplete() {
        RuntimeException failure = new RuntimeException("smtp down");
        doThrow(failure).when(emailVerificationStrategy).sendMessage(anyString(), anyString());

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> listener.sendEmailVerification(testEvent));

        assertSame(failure, thrown);
    }

    @Test
    void shouldPropagatePhoneSendFailureSoPublicationStaysIncomplete() {
        RuntimeException failure = new RuntimeException("whatsapp 404");
        doThrow(failure).when(phoneVerificationStrategy).sendMessage(anyString(), anyString());

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> listener.sendPhoneVerification(testEvent));

        assertSame(failure, thrown);
    }

    @Test
    void emailFailureDoesNotAffectPhoneListener() {
        doThrow(new RuntimeException("smtp down"))
                .when(emailVerificationStrategy).sendMessage(anyString(), anyString());

        assertThrows(RuntimeException.class, () -> listener.sendEmailVerification(testEvent));
        listener.sendPhoneVerification(testEvent);

        verify(phoneVerificationStrategy).sendMessage("+380980137037", "phone-token");
    }
}
