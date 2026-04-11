package com.community.mvp.backend.interfaces.rest.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.community.mvp.backend.infrastructure.security.JwtTokenService;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.infrastructure.persistence.user.entity.CdkEntity;
import com.community.mvp.backend.infrastructure.persistence.user.entity.UserAccountEntity;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaCdkRepository;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaUserAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaUserAccountRepository userAccountRepository;

    @Autowired
    private JpaCdkRepository cdkRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        cdkRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void shouldRegisterLoginAndViewProfile() throws Exception {
        cdkRepository.save(new CdkEntity("CDK-001"));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "alice",
                      "email": "alice@example.com",
                      "password": "Password123",
                      "confirmPassword": "Password123",
                      "cdkCode": "CDK-001"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.username").value("alice"))
            .andExpect(jsonPath("$.data.status").value(1));

        JsonNode loginJson = objectMapper.readTree(mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "alice@example.com",
                      "password": "Password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString());

        String token = loginJson.path("data").path("token").asText();

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("alice"))
            .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    void shouldChangePasswordAndInvalidateOldToken() throws Exception {
        cdkRepository.save(new CdkEntity("CDK-002"));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "bob",
                      "email": "bob@example.com",
                      "password": "Password123",
                      "confirmPassword": "Password123",
                      "cdkCode": "CDK-002"
                    }
                    """))
            .andExpect(status().isOk());

        String token = objectMapper.readTree(mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "bob@example.com",
                      "password": "Password123"
                    }
                    """))
            .andReturn()
            .getResponse()
            .getContentAsString())
            .path("data")
            .path("token")
            .asText();

        mockMvc.perform(post("/api/users/password/change")
                .with(csrf())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "oldPassword": "Password123",
                      "newPassword": "Password456",
                      "confirmNewPassword": "Password456"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("bob"));

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "bob@example.com",
                      "password": "Password123"
                    }
                    """))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "bob@example.com",
                      "password": "Password456"
                    }
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRejectDisabledUserEvenWithValidToken() throws Exception {
        UserAccountEntity user = userAccountRepository.save(new UserAccountEntity(
            "carol",
            "carol@example.com",
            passwordEncoder.encode("Password123"),
            null,
            UserRole.MEMBER.getCode(),
            UserStatus.DISABLED.getCode()
        ));
        String token = jwtTokenService.issueToken(new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getStatus(),
            Instant.now()
        ));

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldRejectDuplicateUsername() throws Exception {
        cdkRepository.save(new CdkEntity("CDK-003"));
        cdkRepository.save(new CdkEntity("CDK-004"));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "dave",
                      "email": "dave@example.com",
                      "password": "Password123",
                      "confirmPassword": "Password123",
                      "cdkCode": "CDK-003"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "dave",
                      "email": "dave2@example.com",
                      "password": "Password123",
                      "confirmPassword": "Password123",
                      "cdkCode": "CDK-004"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Username already exists."));
    }
}

