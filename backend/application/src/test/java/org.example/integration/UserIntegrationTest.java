package org.example.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.util.UUID;
import formula1.notification.service.EmailSender;
import formula1.notification.service.WhatsAppSender;
import org.example.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private WireMockServer notificationServer;

    @Value("${formula1.notification.whatsapp.phone-number-id:1}")
    private String phoneNumberId;

    @MockitoBean
    private EmailSender emailSender;

    @Autowired
    private WhatsAppSender whatsAppSender;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc.perform(post("/api/users/registration")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                    "email": "astashenkova.katya@gmail.com",
                                    "phoneNumber": "+380980137037",
                                    "role": "USER",
                                    "password": "password",
                                    "repeatPassword": "password"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void restoreDefaultStub() {
        notificationServer.resetAll();
        notificationServer.stubFor(
                WireMock.post(urlPathMatching("/.*/messages"))
                .willReturn(ok()));
    }

    @Test
    void register_ShouldStoreRowAndSendVerificationLinks() throws Exception {
        mockMvc.perform(post("/api/users/registration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "k.astashenkova@ukma.edu.ua",
                                    "phoneNumber": "+380980137038",
                                    "role": "USER",
                                    "password": "password",
                                    "repeatPassword": "password"
                                }
                                """))
                .andExpect(status().isCreated());

        String phoneNumber = jdbc.sql("select phone_number from users where email = ?")
                .param(1, "k.astashenkova@ukma.edu.ua")
                .query(String.class)
                .single();
        assertThat(phoneNumber).isEqualTo("+380980137038");

        String userStatus = jdbc.sql("select user_status from users where email = ?")
                .param(1, "k.astashenkova@ukma.edu.ua")
                .query(String.class)
                .single();
        assertThat(userStatus).isEqualTo("PENDING_VERIFICATION");

        String expectedWhatsAppUrl = "/" + phoneNumberId + "/messages";

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                notificationServer.verify(postRequestedFor(urlEqualTo(expectedWhatsAppUrl))
                        .withRequestBody(containing("380980137038"))));

        verify(emailSender, timeout(5000)).sendEmail(
                eq("k.astashenkova@ukma.edu.ua"),
                anyString(),
                contains("/auth/confirm-email?token="));
    }

    @Test
    void registerUserWithInvalidFields_ShouldShowFieldError() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from users")
                .query(Integer.class)
                .single();

        mockMvc.perform(post("/api/users/registration").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "",
                                    "phoneNumber": "+381",
                                    "role": "USER",
                                    "password": "password123",
                                    "repeatPassword": "anotherPassword123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation failed for one or more fields")));

        int rowsChanged = jdbc.sql("select count(*) from users")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    void registerDuplicateUser_ShouldReturnProblemDetail409() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from users")
                .query(Integer.class)
                .single();

        mockMvc.perform(post("/api/users/registration").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "astashenkova.katya@gmail.com",
                                    "phoneNumber": "+3584085857404",
                                    "role": "ADMIN",
                                    "password": "password123",
                                    "repeatPassword": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("User with email astashenkova.katya@gmail.com already exists")));

        int rowsChanged = jdbc.sql("select count(*) from users")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }


    @Test
    void login_ShouldAllowUserToEnter() throws Exception {
        jdbc.sql("update users set user_status = 'ACTIVE' where email = ?")
                .param(1, "astashenkova.katya@gmail.com")
                .update();

        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "astashenkova.katya@gmail.com",
                                    "password": "password"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithWrongPassword_ShouldNotAllowUserToEnter() throws Exception {
        jdbc.sql("update users set user_status = 'ACTIVE' where email = ?")
                .param(1, "astashenkova.katya@gmail.com")
                .update();

        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "astashenkova.katya@gmail.com",
                                    "password": "wrong-paasword"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Bad credentials")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus_ShouldChangeUserStatusSuccessfully() throws Exception {
        UUID userId = jdbc.sql("select id from users where email = ?")
                .param(1, "astashenkova.katya@gmail.com")
                .query(UUID.class)
                .single();

        mockMvc.perform(put("/api/users/{id}/status", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userStatus": "EMAIL_VERIFIED"
                                }
                                """))
                .andExpect(status().isOk());

        String userStatus = jdbc.sql("select user_status from users where id = ?")
                .param(1, userId)
                .query(String.class)
                .single();
        assertThat(userStatus).isEqualTo("EMAIL_VERIFIED");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatusWithInvalidValue_ShouldNotAllowChanging() throws Exception {
        UUID userId = jdbc.sql("select id from users where email = ?")
                .param(1, "astashenkova.katya@gmail.com")
                .query(UUID.class)
                .single();

        String currentUserStatus = jdbc.sql("select user_status from users where id = ?")
                .param(1, userId)
                .query(String.class)
                .single();

        mockMvc.perform(put("/api/users/{id}/status", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userStatus": "ACTIVE"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString(String.format(
                        "Illegal state transition for user '%s' from %s to %s",
                        userId,  currentUserStatus, "ACTIVE"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatusForInvalidUser_ShouldNotAllowChanging() throws Exception {
        UUID nonExistingUserId = UUID.randomUUID();
        mockMvc.perform(put("/api/users/{id}/status", nonExistingUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userStatus": "EMAIL_VERIFIED"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("User with id %s not found", nonExistingUserId))));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserStatusByNotAdmin_ShouldNotAllowChanging() throws Exception {
        UUID userId = jdbc.sql("select id from users where email = ?")
                .param(1, "astashenkova.katya@gmail.com")
                .query(UUID.class)
                .single();

        mockMvc.perform(put("/api/users/{id}/status", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userStatus": "EMAIL_VERIFIED"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));
    }

    @Test
    void confirmAccount_ShouldChangeUserStatusAndDeletePhoneToken() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from verification_tokens")
                .query(Integer.class)
                .single();

        UUID userId = jdbc.sql("select id from users where email = ?")
                .param(1, "astashenkova.katya@gmail.com")
                .query(UUID.class)
                .single();

        String token = jdbc.sql("select token from verification_tokens where user_id = ? AND token_type = 'PHONE_VERIFICATION'")
                .param(1, userId)
                .query(String.class)
                .single();

        mockMvc.perform(post("/api/users/confirmation")
                        .with(csrf())
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String userStatus = jdbc.sql("select user_status from users where id = ?")
                .param(1, userId)
                .query(String.class)
                .single();
        assertThat(userStatus).isEqualTo("PHONE_VERIFIED");


        int rowsChanged = jdbc.sql("select count(*) from verification_tokens")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore - 1);
    }

    @Test
    void confirmAccountWithInvalidToken_ShouldNotInfluence() throws Exception {
        Integer rowsBefore = jdbc.sql("select count(*) from verification_tokens")
                .query(Integer.class)
                .single();

        mockMvc.perform(post("/api/users/confirmation")
                        .with(csrf())
                        .param("token", String.valueOf(111))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid token")));

        int rowsChanged = jdbc.sql("select count(*) from verification_tokens")
                .query(Integer.class)
                .single();

        assertThat(rowsChanged).isEqualTo(rowsBefore);
    }

    @Test
    void loginByNotVerifiedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "astashenkova.katya@gmail.com",
                                    "password": "password"
                                }
                            """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("User is not active")));
    }

    @Test
    void whatsappServerError_ShouldBeReported() {
        notificationServer.stubFor(WireMock.post(urlPathMatching("/.*/messages"))
                .willReturn(serverError()));

        assertThatThrownBy(() -> whatsAppSender.sendMessage(
                "+381111111111",
                "verificaion_link",
                "token123"))
                .isInstanceOf(HttpServerErrorException.class);
    }
}
