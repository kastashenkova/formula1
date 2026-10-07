package formula1.notification.service;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class WhatsAppSender {
    private static final Logger log = LoggerFactory.getLogger(WhatsAppSender.class);

    private final RestTemplate restTemplate;
    private final String whatsappToken;
    private final String phoneNumberId;
    private final String whatsappApiUrl;

    public WhatsAppSender(RestTemplate restTemplate, String whatsappToken,
                          String phoneNumberId, String whatsappApiUrl) {
        this.restTemplate = restTemplate;
        this.whatsappToken = whatsappToken;
        this.phoneNumberId = phoneNumberId;
        this.whatsappApiUrl = whatsappApiUrl;
    }

    public void sendMessage(String to, String templateName, String body) {
        log.info("Sending message from WhatsAppSender starter service to {}", to);

        String url = String.format("%s/%s/messages", whatsappApiUrl, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(whatsappToken);

        String recipient = to.replaceAll("[^0-9]", "");

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", recipient,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", "en"),
                        "components", List.of(Map.of(
                                "type", "button",
                                "sub_type", "url",
                                "index", "0",
                                "parameters", List.of(Map.of(
                                        "type", "text",
                                        "text", body
                                ))
                        ))
                )
        );

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
        } catch (RestClientResponseException e) {
            log.error("WhatsApp API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }
}
