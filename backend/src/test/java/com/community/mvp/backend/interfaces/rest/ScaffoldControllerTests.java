package com.community.mvp.backend.interfaces.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@org.springframework.test.context.TestPropertySource(properties = "community-mvp.security.jwt-enabled=true")
class ScaffoldControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnScaffoldStatus() throws Exception {
        mockMvc.perform(get("/api/v1/scaffold/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.application").value("community-mvp-backend"))
            .andExpect(jsonPath("$.data.databaseEnabled").value(false));
    }

    @Test
    void shouldEchoValidatedMessage() throws Exception {
        mockMvc.perform(post("/api/v1/scaffold/echo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "message": "hello scaffold"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.message").value("hello scaffold"));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/scaffold/echo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "message": " "
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.data.message").value("message must not be blank"));
    }

    @Test
    void actuatorHealthShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void actuatorHealthProbesShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health/readiness"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void securePingShouldRequireBearerTokenAndExposePrincipal() throws Exception {
        mockMvc.perform(get("/api/v1/scaffold/secure-ping"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/scaffold/secure-ping")
                .header("Authorization", "Bearer user-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.authenticated").value(true))
            .andExpect(jsonPath("$.data.principal").value("user-123"));
    }

    @Test
    void shouldRejectMalformedJsonWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/scaffold/echo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REQUEST_BODY_ERROR"))
            .andExpect(jsonPath("$.message").value("Request body is malformed."));
    }
}
