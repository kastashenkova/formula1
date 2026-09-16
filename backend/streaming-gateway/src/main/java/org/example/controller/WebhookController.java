package org.example.controller;

import org.example.dto.webhooks.WebhookRequestDto;
import org.example.dto.webhooks.WebhookResponseDto;
import org.example.service.webhook.WebhookService;
import java.net.URI;
import org.example.validation.OnCreate;
import org.example.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Webhook controller",
        description = "Webhook for upload race info")
@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    @Operation(summary = "Create a new webhook",
            description = "Create a new webhook")
    public ResponseEntity<WebhookResponseDto> createWebhook(
            @Validated({OnCreate.class})
            @RequestBody WebhookRequestDto request
    ) {
        WebhookResponseDto response = webhookService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a certain webhook",
            description = "Get a webhook by its id")
    public ResponseEntity<WebhookResponseDto> getWebhook(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                webhookService.getById(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a certain webhook",
            description = "Update a webhook by its id")
    public ResponseEntity<WebhookResponseDto> updateWebhook(
            @PathVariable Long id,
            @Validated({OnUpdate.class})
            @RequestBody WebhookRequestDto request
    ) {
        return ResponseEntity.ok(
                webhookService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a certain webhook",
            description = "Delete a webhook by its id")
    public ResponseEntity<Void> deleteWebhook(
            @PathVariable Long id
    ) {
        webhookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
