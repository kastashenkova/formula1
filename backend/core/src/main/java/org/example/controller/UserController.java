package org.example.controller;

import java.net.URI;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.user.registration.UserRegistrationRequestDto;
import org.example.dto.user.registration.UserRegistrationResponseDto;
import org.example.exception.RegistrationException;
import org.example.service.user.UserService;
import org.example.validation.OnCreate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Authentification management",
        description = "Endpoints for managing user registration and authentification")
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "User registration",
            description = "Create a new user")
    public ResponseEntity<UserRegistrationResponseDto> register(@Validated(OnCreate.class)
                                        @RequestBody
                                        UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        UserRegistrationResponseDto created = userService.addUser(requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }
}
