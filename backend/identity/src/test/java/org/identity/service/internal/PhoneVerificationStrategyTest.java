package org.identity.service.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import formula1.notification.service.WhatsAppSender;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.Role;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;
import org.identity.exception.InvalidUserStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationStrategyTest {

    @Mock
    private WhatsAppSender whatsAppSender;

    private PhoneVerificationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PhoneVerificationStrategy(whatsAppSender);
        ReflectionTestUtils.setField(strategy, "expiryHours", 24L);
    }

    @Test
    void shouldGetPhoneTokenType() {
        TokenType type = strategy.getVerificationTokenType();

        assertNotNull(type);
        assertEquals(TokenType.PHONE_VERIFICATION, type);
    }

    @Test
    void shouldCreateTokenWithCorrectExpiry() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        VerificationToken token = strategy.createVerificationToken(testUser);

        assertEquals(TokenType.PHONE_VERIFICATION.toString(), token.getTokenType());
        assertEquals(testUser, token.getUser());
        assertNotNull(token.getToken());
        assertTrue(token.getExpiryDate().isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    void shouldSendMessage() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        VerificationToken token = new VerificationToken(
                1L,
                testUser,
                "test-token",
                TokenType.PHONE_VERIFICATION.toString(),
                LocalDateTime.now());

        String phoneNumber = "+380980137037";

        strategy.sendMessage(phoneNumber, token.getToken());

        verify(whatsAppSender, times(1))
                .sendMessage(eq(testUser.getPhoneNumber()),
                        anyString(),
                        anyString());
    }

    @Test
    void shouldThrowExceptionWhenWhatsAppApiFails() {
        UserEntity testUser = new UserEntity(
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN.toString(),
                "admin123",
                UserStatus.PHONE_VERIFIED.toString());

        VerificationToken token = new VerificationToken(
                1L,
                testUser,
                "test-token",
                TokenType.PHONE_VERIFICATION.toString(),
                LocalDateTime.now());


        RestClientResponseException mockException = new RestClientResponseException(
                "Bad Request", 400, "Bad Request", null, "Invalid phone number".getBytes(), null);

        doThrow(mockException)
                .when(whatsAppSender)
                .sendMessage(eq(testUser.getPhoneNumber()), anyString(), eq(token.getToken()));

        assertThrows(RestClientResponseException.class,
                () -> strategy.sendMessage(testUser.getPhoneNumber(), token.getToken()));

        verify(whatsAppSender, times(1))
                .sendMessage(eq(testUser.getPhoneNumber()), eq("verificaion_link"), eq(token.getToken()));
    }

    @Test
    void shouldReturnPhoneVerifiedStatusWhenCurrentIsPendingVerification() {
        UserStatus currentStatus = UserStatus.PENDING_VERIFICATION;

        UserStatus actualStatus = strategy.getNextStatus(currentStatus);

        assertNotNull(actualStatus);
        assertEquals(UserStatus.PHONE_VERIFIED, actualStatus);
    }

    @Test
    void shouldReturnActiveStatusWhenCurrentIsEmailVerified() {
        UserStatus currentStatus = UserStatus.EMAIL_VERIFIED;

        UserStatus actualStatus = strategy.getNextStatus(currentStatus);

        assertNotNull(actualStatus);
        assertEquals(UserStatus.ACTIVE, actualStatus);
    }

    @Test
    void shouldThrowExceptionWhenCurrentStatusIsInvalid() {
        UserStatus currentStatus = UserStatus.ACTIVE;

        assertThrows(InvalidUserStateException.class, () -> strategy.getNextStatus(currentStatus));
    }
}
