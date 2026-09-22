package org.example.service.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationStrategyTest {

    @Mock
    private RestTemplate restTemplate;

    private PhoneVerificationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PhoneVerificationStrategy(restTemplate);
        ReflectionTestUtils.setField(strategy, "expiryHours", 24L);
        ReflectionTestUtils.setField(strategy, "whatsappToken", "whatsapp-token");
        ReflectionTestUtils.setField(strategy, "phoneNumberId", "1111111111111111");
        ReflectionTestUtils.setField(strategy, "whatsappApiUrl", "https://graph.facebook.com/v25.0");
    }

    @Test
    void shouldGetPhoneTokenType() {
        TokenType type = strategy.getVerificationTokenType();

        assertNotNull(type);
        assertEquals(TokenType.PHONE_VERIFICATION, type);
    }

    @Test
    void shouldCreateTokenWithCorrectExpiry() {
        UUID userId = UUID.randomUUID();
        VerificationToken token = strategy.createVerificationToken(userId);

        assertEquals(TokenType.PHONE_VERIFICATION, token.tokenType());
        assertEquals(userId, token.userId());
        assertNotNull(token.token());
        assertTrue(token.expiryDate().isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    void shouldSendMessage() {
        VerificationToken token = new VerificationToken(
                UUID.randomUUID(),
                "test-token",
                TokenType.PHONE_VERIFICATION,
                LocalDateTime.now());

        String phoneNumber = "+380980137037";
        String expectedUrl = "https://graph.facebook.com/v25.0/1111111111111111/messages";

        when(restTemplate.postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok().build());

        strategy.sendMessage(phoneNumber, token);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).postForEntity(eq(expectedUrl), entityCaptor.capture(), eq(String.class));

        HttpEntity<Map<String, Object>> capturedEntity = entityCaptor.getValue();

        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals("Bearer whatsapp-token", headers.getFirst(HttpHeaders.AUTHORIZATION));

        Map<String, Object> payload = capturedEntity.getBody();
        assertNotNull(payload);
        assertEquals("whatsapp", payload.get("messaging_product"));
        assertEquals("380980137037", payload.get("to"));
        assertEquals("template", payload.get("type"));

        Map<String, Object> template = (Map<String, Object>) payload.get("template");
        assertEquals("verificationlink", template.get("name"));

        List<Map<String, Object>> components = (List<Map<String, Object>>) template.get("components");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) components.get(0).get("parameters");

        assertEquals("test-token", parameters.getFirst().get("text"));
    }

    @Test
    void shouldThrowExceptionWhenWhatsAppApiFails() {
        VerificationToken token = new VerificationToken(
                UUID.randomUUID(), "test-token", TokenType.PHONE_VERIFICATION, LocalDateTime.now());
        String expectedUrl = "https://graph.facebook.com/v25.0/1111111111111111/messages";

        RestClientResponseException mockException = new RestClientResponseException(
                "Bad Request", 400, "Bad Request", null, "Invalid phone number".getBytes(), null);

        when(restTemplate.postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(String.class)))
                .thenThrow(mockException);

        assertThrows(RestClientResponseException.class,
                () -> strategy.sendMessage("+380980137037", token));
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
