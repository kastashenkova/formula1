package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.identity.command.UpdateUserStatusCommand;
import org.identity.dto.UserLoginRequestDto;
import org.identity.dto.UserLoginResponseDto;
import org.identity.dto.UserRegistrationRequestDto;
import org.identity.dto.UserResponseDto;
import org.identity.exception.RegistrationException;
import org.identity.service.UserService;
import org.identity.service.AuthenticationService;
import org.identity.validation.OnCreate;
import org.identity.validation.OnUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Authentification management",
        description = "Endpoints for managing user registration and authentification")
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(UserService userService,
                          AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/registration")
    @Operation(summary = "User registration",
            description = "Create a new user")
    public ResponseEntity<UserResponseDto> register(@Validated(OnCreate.class)
                                        @RequestBody UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        UserResponseDto created = userService.addUser(requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/login")
    @Operation(summary = "User authentication",
            description = "Authenticate an existing user")
    public ResponseEntity<UserLoginResponseDto> login(@RequestBody @Valid UserLoginRequestDto request) {
        UserLoginResponseDto logged = authenticationService.authenticate(request);

        return ResponseEntity.ok(logged);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status",
            description = "Update user status by id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @PathVariable UUID id,
            @Validated(OnUpdate.class)
            @RequestBody UpdateUserStatusCommand updateUserStatusCommand) {
        UserResponseDto updatedUser = userService.updateStatus(id, updateUserStatusCommand);

        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/confirmation")
    @Operation(summary = "Confirm account",
            description = "Activates user account via token")
    public ResponseEntity<Void> confirmAccount(@RequestParam("token") String token) {
        userService.confirmByToken(token);

        return ResponseEntity.ok().build();
    }
}
