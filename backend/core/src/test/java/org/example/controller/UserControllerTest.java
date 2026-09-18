package org.example.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.user.registration.UserRegistrationRequestDto;
import org.example.dto.user.registration.UserRegistrationResponseDto;
import org.example.enums.Role;
import org.example.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@TestConstructor(autowireMode = ALL)
public class UserControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
                Role.USER,
                "user1234",
                "user1234"
        );

        UserRegistrationResponseDto newUserResponseDto = new UserRegistrationResponseDto(
                1L,
                newUserRequestDto.email(),
                newUserRequestDto.phoneNumber(),
                newUserRequestDto.role()
        );

        when(userService.addUser(newUserRequestDto)).thenReturn(newUserResponseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(newUserRequestDto.email()));

        verify(userService, times(1)).addUser(newUserRequestDto);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is invalid")
    void register_invalidEmail_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto invalidRequest = new UserRegistrationRequestDto(
                null,
                "invalid-email-format",
                "+380980137037",
                Role.USER,
                "user1234",
                "user1234"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password and repeat password do not match")
    void register_passwordAndRepeatPasswordNotMatch_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto invalidRequest = new UserRegistrationRequestDto(
                null,
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin123",
                "user1234"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password length is less than 8 symbols")
    void register_passwordTooSmall_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto invalidRequest = new UserRegistrationRequestDto(
                null,
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin12",
                "admin12"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password length is more than 35 symbols")
    void register_passwordTooLong_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto invalidRequest = new UserRegistrationRequestDto(
                null,
                "d.dzhos@ukma.edu.ua",
                "+380980137037",
                Role.ADMIN,
                "admin1234567890_admin1234567890_admin1234567890",
                "admin1234567890_admin1234567890_admin1234567890"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when role is not USER or ADMIN")
    void register_notExistingRole_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "k.astashenkova@ukma.edu.ua",
                                "role": "not-existing-role",
                                "password": "admin123" }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}
