package com.community.mvp.backend.interfaces.rest.content.course;

import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.infrastructure.persistence.content.repository.jpa.JpaCourseChapterRepository;
import com.community.mvp.backend.infrastructure.persistence.content.repository.jpa.JpaCourseRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JpaUserAccountRepository userAccountRepository;

    @Autowired
    private JpaCdkRepository cdkRepository;

    @Autowired
    private JpaCourseRepository courseRepository;

    @Autowired
    private JpaCourseChapterRepository chapterRepository;

    private UserAccountEntity adminUser;
    private UserAccountEntity memberUser;

    @BeforeEach
    void setUp() {
        chapterRepository.deleteAll();
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

        memberUser = userAccountRepository.save(new UserAccountEntity(
            "member-user",
            "member@example.com",
            "hashed-password",
            null,
            UserRole.MEMBER.getCode(),
            UserStatus.ACTIVE.getCode()
        ));
    }

    @Test
    void adminShouldCreateCourseAndMemberShouldReadPublishedCourse() throws Exception {
        JsonNode createdJson = objectMapper.readTree(mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", bearerToken(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Spring Boot 内容实战",
                      "description": "用于会员学习的课程",
                      "coverImage": "https://example.com/cover.png",
                      "status": 1,
                      "chapters": [
                        {"title": "第二章", "content": "# chapter2", "sortOrder": 2},
                        {"title": "第一章", "content": "# chapter1", "sortOrder": 1}
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.status").value(1))
            .andExpect(jsonPath("$.data.chapterCount").value(2))
            .andReturn()
            .getResponse()
            .getContentAsString());

        long courseId = createdJson.path("data").path("courseId").asLong();

        mockMvc.perform(get("/api/courses")
                .header("Authorization", bearerToken(memberUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].id").value(courseId))
            .andExpect(jsonPath("$.data[0].chapterCount").value(2));

        mockMvc.perform(get("/api/courses/{id}", courseId)
                .header("Authorization", bearerToken(memberUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.chapters[0].sortOrder").value(1))
            .andExpect(jsonPath("$.data.chapters[1].sortOrder").value(2));
    }

    @Test
    void memberShouldNotCreateCourse() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", bearerToken(memberUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "非管理员创建",
                      "description": "should fail",
                      "status": 1,
                      "chapters": []
                    }
                    """))
                    .andExpect(status().is4xxClientError())
                    .andExpect(jsonPath("$.code", anyOf(is("FORBIDDEN"), is("UNAUTHORIZED"))));
    }

    @Test
    void adminShouldUpdateAndSoftDeleteCourse() throws Exception {
        long courseId = createCourseByAdminAndReturnId("可编辑课程", 1);

        mockMvc.perform(put("/api/admin/courses/{id}", courseId)
                .header("Authorization", bearerToken(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "已编辑课程",
                      "description": "更新后的课程描述",
                      "coverImage": "https://example.com/updated.png",
                      "status": 1
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courseId").value(courseId))
            .andExpect(jsonPath("$.data.status").value(1));

        mockMvc.perform(get("/api/courses")
                .header("Authorization", bearerToken(memberUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].title").value("已编辑课程"));

        mockMvc.perform(delete("/api/admin/courses/{id}", courseId)
                .header("Authorization", bearerToken(adminUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courseId").value(courseId))
            .andExpect(jsonPath("$.data.status").value(2));

        mockMvc.perform(get("/api/courses")
                .header("Authorization", bearerToken(memberUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void adminShouldUpdateCourseChapter() throws Exception {
        JsonNode createdJson = objectMapper.readTree(mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", bearerToken(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "章节编辑课程",
                      "description": "desc",
                      "status": 1,
                      "chapters": [
                        {"title": "第一章", "content": "old", "sortOrder": 1},
                        {"title": "第二章", "content": "second", "sortOrder": 2}
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        long courseId = createdJson.path("data").path("courseId").asLong();

        JsonNode detailJson = objectMapper.readTree(mockMvc.perform(get("/api/courses/{id}", courseId)
                .header("Authorization", bearerToken(memberUser)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        long chapterId = detailJson.path("data").path("chapters").get(0).path("id").asLong();

        mockMvc.perform(put("/api/admin/courses/{id}/chapters/{chapterId}", courseId, chapterId)
                .header("Authorization", bearerToken(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "第一章-更新",
                      "content": "new-content",
                      "sortOrder": 3
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courseId").value(courseId))
            .andExpect(jsonPath("$.data.chapterId").value(chapterId))
            .andExpect(jsonPath("$.data.sortOrder").value(3));

        mockMvc.perform(get("/api/courses/{id}/chapters/{chapterId}", courseId, chapterId)
                .header("Authorization", bearerToken(memberUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("第一章-更新"))
            .andExpect(jsonPath("$.data.content").value("new-content"))
            .andExpect(jsonPath("$.data.sortOrder").value(3));
    }

    @Test
    void memberShouldNotUpdateCourseChapter() throws Exception {
        JsonNode createdJson = objectMapper.readTree(mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", bearerToken(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "章节权限课程",
                      "description": "desc",
                      "status": 1,
                      "chapters": [
                        {"title": "第一章", "content": "old", "sortOrder": 1}
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        long courseId = createdJson.path("data").path("courseId").asLong();

        JsonNode detailJson = objectMapper.readTree(mockMvc.perform(get("/api/courses/{id}", courseId)
                .header("Authorization", bearerToken(memberUser)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        long chapterId = detailJson.path("data").path("chapters").get(0).path("id").asLong();

        mockMvc.perform(put("/api/admin/courses/{id}/chapters/{chapterId}", courseId, chapterId)
                .header("Authorization", bearerToken(memberUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "越权更新",
                      "content": "should fail",
                      "sortOrder": 1
                    }
                    """))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code", anyOf(is("FORBIDDEN"), is("UNAUTHORIZED"))));
    }

    @Test
    void memberShouldNotUpdateOrDeleteCourse() throws Exception {
        long courseId = createCourseByAdminAndReturnId("受保护课程", 1);

        mockMvc.perform(put("/api/admin/courses/{id}", courseId)
                .header("Authorization", bearerToken(memberUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "越权编辑",
                      "description": "should fail",
                      "status": 1
                    }
                    """))
                    .andExpect(status().is4xxClientError())
                    .andExpect(jsonPath("$.code", anyOf(is("FORBIDDEN"), is("UNAUTHORIZED"))));

        mockMvc.perform(delete("/api/admin/courses/{id}", courseId)
                .header("Authorization", bearerToken(memberUser)))
                    .andExpect(status().is4xxClientError())
                    .andExpect(jsonPath("$.code", anyOf(is("FORBIDDEN"), is("UNAUTHORIZED"))));
    }

    @Test
    void listShouldOnlyReturnPublishedCoursesAndExcludeOffline() throws Exception {
        createCourseByAdmin("草稿课程", 0);
        createCourseByAdmin("已发布课程", 1);
        createCourseByAdmin("已下架课程", 2);

        mockMvc.perform(get("/api/courses")
                .header("Authorization", bearerToken(memberUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].title").value("已发布课程"));
    }

    private long createCourseByAdminAndReturnId(String title, int status) throws Exception {
        JsonNode createdJson = objectMapper.readTree(mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", bearerToken(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "%s",
                      "description": "desc",
                      "status": %d,
                      "chapters": [
                        {"title": "chapter", "content": "content", "sortOrder": 1}
                      ]
                    }
                    """.formatted(title, status)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
        return createdJson.path("data").path("courseId").asLong();
    }

    private void createCourseByAdmin(String title, int status) throws Exception {
        createCourseByAdminAndReturnId(title, status);
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
