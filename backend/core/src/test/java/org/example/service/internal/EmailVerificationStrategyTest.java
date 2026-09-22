package org.example.service.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;
import org.example.entity.VerificationToken;
import org.example.enums.TokenType;
import org.example.enums.UserStatus;
import org.example.exception.InvalidUserStateException;
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
        UUID userId = UUID.randomUUID();
        VerificationToken token = strategy.createVerificationToken(userId);

        assertEquals(TokenType.EMAIL_VERIFICATION, token.tokenType());
        assertEquals(userId, token.userId());
        assertNotNull(token.token());
        assertTrue(token.expiryDate().isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    void shouldSendMessage() {
        VerificationToken token = new VerificationToken(
                UUID.randomUUID(),
                "test-token",
                TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now());

        strategy.sendMessage("k.astashenkova@ukma.edu.ua", token);

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
