package org.identity.service.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.Role;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;
import org.identity.exception.InvalidUserStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailVerificationStrategyTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailVerificationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new EmailVerificationStrategy(mailSender);
        ReflectionTestUtils.setField(strategy, "expiryHours", 24L);
        ReflectionTestUtils.setField(strategy, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void shouldGetEmailTokenType() {
        TokenType type = strategy.getVerificationTokenType();

        assertNotNull(type);
        assertEquals(TokenType.EMAIL_VERIFICATION, type);
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

        assertEquals(TokenType.EMAIL_VERIFICATION.toString(), token.getTokenType());
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
                TokenType.EMAIL_VERIFICATION.toString(),
                LocalDateTime.now());

        strategy.sendMessage("k.astashenkova@ukma.edu.ua", token.getToken());

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assert sentMessage.getTo() != null;
        assertEquals("k.astashenkova@ukma.edu.ua", sentMessage.getTo()[0]);
        assert sentMessage.getText() != null;
        assertTrue(sentMessage.getText().contains("token=test-token"));
    }

    @Test
    void shouldReturnEmailVerifiedStatusWhenCurrentIsPendingVerification() {
        UserStatus currentStatus = UserStatus.PENDING_VERIFICATION;

        UserStatus actualStatus = strategy.getNextStatus(currentStatus);

        assertNotNull(actualStatus);
        assertEquals(UserStatus.EMAIL_VERIFIED, actualStatus);
    }

    @Test
    void shouldReturnActiveStatusWhenCurrentIsPhoneVerified() {
        UserStatus currentStatus = UserStatus.PHONE_VERIFIED;

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
