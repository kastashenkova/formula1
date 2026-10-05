package org.example.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.identity.dto.WebhookRequestDto;
import org.identity.dto.WebhookResponseDto;
import org.identity.service.WebhookService;
import java.net.URI;
import org.identity.validation.OnCreate;
import org.identity.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Webhook created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data request", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User specified in request not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook found"),
            @ApiResponse(responseCode = "404", description = "Webhook not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data request", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Webhook not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Webhook deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Webhook not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> deleteWebhook(
            @PathVariable Long id
    ) {
        webhookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
