package example.controller;

import example.dto.webhooks.RequestWebhookDTO;
import example.dto.webhooks.ResponseWebhookDTO;
import example.service.streaming.webhooks.WebhookService;
import java.net.URI;

import example.validation.OnCreate;
import example.validation.OnUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@ControllerAdvice
@RequestMapping("/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<ResponseWebhookDTO> createWebhook(
            @Validated({OnCreate.class})
            @RequestBody RequestWebhookDTO request
    ) {
        ResponseWebhookDTO response = webhookService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/webhooks/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWebhookDTO> getWebhook(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                webhookService.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseWebhookDTO> updateWebhook(
            @PathVariable Long id,
            @Validated({OnUpdate.class})
            @RequestBody RequestWebhookDTO request
    ) {
        return ResponseEntity.ok(
                webhookService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(
            @PathVariable Long id
    ) {
        webhookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}