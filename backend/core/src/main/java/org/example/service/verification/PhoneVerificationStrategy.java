package org.example.service.verification;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.example.entity.VerificationToken;
import org.example.enums.TokenType;
import org.example.enums.UserStatus;
import org.example.exception.InvalidUserStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PhoneVerificationStrategy implements VerificationStrategy {

    @Value("${phone.token.expiry.date}")
    private long expiryHours;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${whatsapp.api.token}")
    private String whatsappToken;

    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.api.url}")
    private String whatsappApiUrl;

    private final RestTemplate restTemplate;

    public PhoneVerificationStrategy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public TokenType getVerificationTokenType() {
        return TokenType.PHONE_VERIFICATION;
    }

    @Override
    public VerificationToken createVerificationToken(UUID userId) {
        String token = UUID.randomUUID().toString();

        LocalDateTime expirationTime = LocalDateTime.now().plusHours(expiryHours);

        return new VerificationToken(
                userId,
                token,
                TokenType.PHONE_VERIFICATION,
                expirationTime
        );
    }

    @Override
    public void sendMessage(String to, VerificationToken token) {
        String confirmationUrl = frontendUrl + "/auth/confirm-phone-number?token=" + token.token();
        String messageBody = "Click the link to confirm your phone number to use account in Formula1 App: " + confirmationUrl;

        String url = String.format("%s/%s/messages", whatsappApiUrl, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(whatsappToken);

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "text",
                "text", Map.of("body", messageBody)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(url, request, String.class);
    }

    @Override
    public UserStatus getNextStatus(UserStatus currentStatus) {
        if (currentStatus == UserStatus.PENDING_VERIFICATION) {
            return UserStatus.PHONE_VERIFIED;
        }
        if (currentStatus == UserStatus.EMAIL_VERIFIED) {
            return UserStatus.ACTIVE;
        }

        throw new InvalidUserStateException("Phone cannot be verified from status: " + currentStatus);
    }
}
