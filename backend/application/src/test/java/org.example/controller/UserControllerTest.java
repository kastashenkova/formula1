package org.example.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.identity.dto.UserRegistrationRequestDto;
import org.identity.dto.UserResponseDto;
import org.identity.enums.Role;
import org.identity.enums.UserStatus;
import org.identity.security.JwtUtil;
import org.identity.service.AuthenticationService;
import org.identity.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = ALL)
public class UserControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("Should add new User")
    void register_newUser_Success() throws Exception {
        UserRegistrationRequestDto newUserRequestDto = new UserRegistrationRequestDto(
                null,
                "k.astashenkova@ukma.edu.ua",
                "+380980137037",
                Role.USER.toString(),
                "user1234",
                "user1234"
        );

        UserResponseDto newUserResponseDto = new UserResponseDto(
                UUID.randomUUID(),
                newUserRequestDto.email(),
                newUserRequestDto.phoneNumber(),
                newUserRequestDto.role(),
                UserStatus.PENDING_VERIFICATION.toString()
        );

        when(userService.addUser(newUserRequestDto)).thenReturn(newUserResponseDto);

        mockMvc.perform(post("/api/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newUserResponseDto.id().toString()))
                .andExpect(jsonPath("$.email").value(newUserRequestDto.email()));

        verify(userService, times(1)).addUser(newUserRequestDto);
    }
}
