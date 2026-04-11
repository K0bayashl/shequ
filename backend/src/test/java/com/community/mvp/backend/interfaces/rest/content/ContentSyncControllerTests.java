package com.community.mvp.backend.interfaces.rest.content;

import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.infrastructure.persistence.user.entity.CdkEntity;
import com.community.mvp.backend.infrastructure.persistence.user.entity.UserAccountEntity;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaCdkRepository;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaUserAccountRepository;
import com.community.mvp.backend.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContentSyncControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JpaUserAccountRepository userAccountRepository;

    @Autowired
    private JpaCdkRepository cdkRepository;

    private UserAccountEntity authUser;

    @BeforeEach
    void setUp() {
        cdkRepository.deleteAll();
        userAccountRepository.deleteAll();

        UserAccountEntity activeOne = userAccountRepository.save(new UserAccountEntity(
            "active-one",
            "active.one@example.com",
            "hashed-password",
            null,
            UserRole.MEMBER.getCode(),
            UserStatus.ACTIVE.getCode()
        ));
        this.authUser = activeOne;
        userAccountRepository.save(new UserAccountEntity(
            "active-two",
            "active.two@example.com",
            "hashed-password",
            null,
            UserRole.MEMBER.getCode(),
            UserStatus.ACTIVE.getCode()
        ));
        userAccountRepository.save(new UserAccountEntity(
            "inactive-one",
            "inactive.one@example.com",
            "hashed-password",
            null,
            UserRole.MEMBER.getCode(),
            UserStatus.INACTIVE.getCode()
        ));

        CdkEntity usedCdk = new CdkEntity("CDK-USED-001");
        usedCdk.markUsed(activeOne.getId(), LocalDateTime.of(2026, 4, 1, 9, 30));
        cdkRepository.save(usedCdk);

        cdkRepository.save(new CdkEntity("CDK-UNUSED-001"));
        cdkRepository.save(new CdkEntity("CDK-UNUSED-002"));
    }

    @Test
    void contentEndpointsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/content/community/feed"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/content/admin/cdks"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/content/docs/chapters"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCommunityFeed() throws Exception {
        mockMvc.perform(get("/api/v1/content/community/feed")
                .param("filter", "official")
                .param("sort", "hot")
                .header("Authorization", bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.threads[0].type").value("official"))
            .andExpect(jsonPath("$.data.topics[0].name").value("React 19"));
    }

    @Test
    void shouldReturnAdminCdkOverview() throws Exception {
        mockMvc.perform(get("/api/v1/content/admin/cdks")
                .param("status", "unused")
                .header("Authorization", bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.totalUsers").value(3))
            .andExpect(jsonPath("$.data.activeMembers").value(2))
            .andExpect(jsonPath("$.data.remainingCdks").value(2))
            .andExpect(jsonPath("$.data.cdks[0].status").value("unused"));
    }

        @Test
        void shouldFilterAdminCdkBySearchKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/content/admin/cdks")
            .param("status", "all")
            .param("search", "active.one@example.com")
            .header("Authorization", bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.cdks.length()").value(1))
            .andExpect(jsonPath("$.data.cdks[0].status").value("used"))
            .andExpect(jsonPath("$.data.cdks[0].usedByEmail").value("active.one@example.com"));
        }

    @Test
    void shouldReturnDocsChapters() throws Exception {
        mockMvc.perform(get("/api/v1/content/docs/chapters")
                .header("Authorization", bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.chapters[0].title").value("Getting Started"))
            .andExpect(jsonPath("$.data.chapters[0].iconKey").value("book"));
    }

    private String bearerToken() {
        String token = jwtTokenService.issueToken(new UserPrincipal(
            authUser.getId(),
            authUser.getUsername(),
            authUser.getEmail(),
            authUser.getRole(),
            authUser.getStatus(),
            Instant.now()
        ));

        return "Bearer " + token;
    }
}
