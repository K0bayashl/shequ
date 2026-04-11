package com.community.mvp.backend.interfaces.rest.moderation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.community.mvp.backend.application.content.command.CreateCourseChapterCommand;
import com.community.mvp.backend.application.content.command.CreateCourseCommand;
import com.community.mvp.backend.application.content.service.ContentCourseService;
import com.community.mvp.backend.application.content.service.CreateCourseResult;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.infrastructure.persistence.content.repository.jpa.JpaCourseChapterRepository;
import com.community.mvp.backend.infrastructure.persistence.content.repository.jpa.JpaCourseRepository;
import com.community.mvp.backend.infrastructure.persistence.moderation.repository.jpa.JpaActionAuditLogRepository;
import com.community.mvp.backend.infrastructure.persistence.moderation.repository.jpa.JpaContentReportRepository;
import com.community.mvp.backend.infrastructure.persistence.user.entity.UserAccountEntity;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaCdkRepository;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaUserAccountRepository;
import com.community.mvp.backend.infrastructure.security.JwtTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModerationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JpaActionAuditLogRepository actionAuditLogRepository;

    @Autowired
    private JpaContentReportRepository contentReportRepository;

    @Autowired
    private JpaCourseChapterRepository courseChapterRepository;

    @Autowired
    private JpaCourseRepository courseRepository;

    @Autowired
    private JpaCdkRepository cdkRepository;

    @Autowired
    private JpaUserAccountRepository userAccountRepository;

    @Autowired
    private ContentCourseService contentCourseService;

    private UserAccountEntity adminUser;
    private UserAccountEntity courseAuthor;
    private UserAccountEntity reporterUser;

    @BeforeEach
    void setUp() {
        actionAuditLogRepository.deleteAll();
        contentReportRepository.deleteAll();
        courseChapterRepository.deleteAll();
        courseRepository.deleteAll();
        cdkRepository.deleteAll();
        userAccountRepository.deleteAll();

        adminUser = userAccountRepository.save(new UserAccountEntity(
            "admin-user",
            "admin@example.com",
            "hashed-password",
            null,
            UserRole.ADMIN.getCode(),
            UserStatus.ACTIVE.getCode()
        ));

        courseAuthor = userAccountRepository.save(new UserAccountEntity(
            "author-user",
            "author@example.com",
            "hashed-password",
            null,
            UserRole.MEMBER.getCode(),
            UserStatus.ACTIVE.getCode()
        ));

        reporterUser = userAccountRepository.save(new UserAccountEntity(
            "reporter-user",
            "reporter@example.com",
            "hashed-password",
            null,
            UserRole.MEMBER.getCode(),
            UserStatus.ACTIVE.getCode()
        ));
    }

    @Test
    void shouldSubmitReportAndHandleWithTakedownAndBan() throws Exception {
        long courseId = createCourseByAuthor();

        JsonNode reportJson = objectMapper.readTree(mockMvc.perform(post("/api/reports")
                .header("Authorization", bearerToken(reporterUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "courseId": %d,
                      "reasonCode": "spam",
                      "reasonDetail": "contains suspicious links"
                    }
                    """.formatted(courseId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.status").value(0))
            .andReturn()
            .getResponse()
            .getContentAsString());

        long reportId = reportJson.path("data").path("reportId").asLong();

        mockMvc.perform(post("/api/admin/moderation/reports/{id}/handle", reportId)
                .header("Authorization", bearerToken(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "decision": "approve",
                      "handleNote": "violation confirmed",
                      "takedownCourse": true,
                      "banAuthor": true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value(1))
            .andExpect(jsonPath("$.data.courseTakenDown").value(true))
            .andExpect(jsonPath("$.data.authorBanned").value(true));

        mockMvc.perform(get("/api/courses")
                .header("Authorization", bearerToken(reporterUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", bearerToken(courseAuthor)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void nonAdminShouldNotHandleReport() throws Exception {
        long courseId = createCourseByAuthor();

        JsonNode reportJson = objectMapper.readTree(mockMvc.perform(post("/api/reports")
                .header("Authorization", bearerToken(reporterUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "courseId": %d,
                      "reasonCode": "abuse",
                      "reasonDetail": "bad language"
                    }
                    """.formatted(courseId)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        long reportId = reportJson.path("data").path("reportId").asLong();

        mockMvc.perform(post("/api/admin/moderation/reports/{id}/handle", reportId)
                .header("Authorization", bearerToken(reporterUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "decision": "reject"
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminShouldListReportsByStatus() throws Exception {
        long courseId = createCourseByAuthor();
        mockMvc.perform(post("/api/reports")
                .header("Authorization", bearerToken(reporterUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "courseId": %d,
                      "reasonCode": "other",
                      "reasonDetail": "for list test"
                    }
                    """.formatted(courseId)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/moderation/reports")
                .header("Authorization", bearerToken(adminUser))
                .param("status", "pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].status").value(0));
    }

    private long createCourseByAuthor() {
        CreateCourseResult result = contentCourseService.createCourse(new CreateCourseCommand(
            "治理测试课程",
            "用于治理切片测试",
            null,
            1,
            java.util.List.of(new CreateCourseChapterCommand("章节一", "content", 1)),
            courseAuthor.getId()
        ));

        return result.courseId();
    }

    private String bearerToken(UserAccountEntity user) {
        String token = jwtTokenService.issueToken(new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getStatus(),
            Instant.now()
        ));
        return "Bearer " + token;
    }
}
