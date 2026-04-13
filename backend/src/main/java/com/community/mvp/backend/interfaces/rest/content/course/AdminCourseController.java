package com.community.mvp.backend.interfaces.rest.content.course;

import com.community.mvp.backend.application.content.command.CreateCourseChapterCommand;
import com.community.mvp.backend.application.content.command.CreateCourseCommand;
import com.community.mvp.backend.application.content.command.DeleteCourseCommand;
import com.community.mvp.backend.application.content.command.UpdateCourseChapterCommand;
import com.community.mvp.backend.application.content.command.UpdateCourseCommand;
import com.community.mvp.backend.application.content.service.ContentCourseService;
import com.community.mvp.backend.application.content.service.CreateCourseResult;
import com.community.mvp.backend.application.content.service.DeleteCourseResult;
import com.community.mvp.backend.application.content.service.UpdateCourseChapterResult;
import com.community.mvp.backend.application.content.service.UpdateCourseResult;
import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.infrastructure.security.CurrentUserContext;
import com.community.mvp.backend.interfaces.rest.content.course.dto.CreateCourseRequest;
import com.community.mvp.backend.interfaces.rest.content.course.dto.CreateCourseResponse;
import com.community.mvp.backend.interfaces.rest.content.course.dto.DeleteCourseResponse;
import com.community.mvp.backend.interfaces.rest.content.course.dto.UpdateCourseChapterRequest;
import com.community.mvp.backend.interfaces.rest.content.course.dto.UpdateCourseChapterResponse;
import com.community.mvp.backend.interfaces.rest.content.course.dto.UpdateCourseRequest;
import com.community.mvp.backend.interfaces.rest.content.course.dto.UpdateCourseResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端课程维护接口。
 * <p>当前提供课程与章节的创建、编辑与软删除能力，要求管理员身份。</p>
 */
@RestController
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    private final ContentCourseService contentCourseService;
    private final CurrentUserContext currentUserContext;

    /**
     * 构造管理端课程控制器。
     *
     * @param contentCourseService 课程应用服务
     * @param currentUserContext 当前用户上下文
     */
    public AdminCourseController(ContentCourseService contentCourseService, CurrentUserContext currentUserContext) {
        this.contentCourseService = contentCourseService;
        this.currentUserContext = currentUserContext;
    }

    /**
     * 创建课程。
     *
     * @param request 创建课程请求
     * @return 新建课程摘要信息
     */
    @PostMapping
    public ApiResponse<CreateCourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        UserPrincipal currentUser = requireAdmin();

        List<CreateCourseChapterCommand> chapterCommands = request.chapters() == null
            ? List.of()
            : request.chapters().stream()
                .map(chapter -> new CreateCourseChapterCommand(chapter.title(), chapter.content(), chapter.sortOrder()))
                .toList();

        CreateCourseResult result = contentCourseService.createCourse(new CreateCourseCommand(
            request.title(),
            request.description(),
            request.coverImage(),
            request.status(),
            chapterCommands,
            currentUser.userId()
        ));

        return ApiResponse.success(new CreateCourseResponse(result.courseId(), result.status(), result.chapterCount()));
    }

    /**
     * 编辑课程基础信息。
     *
     * @param id 课程 ID
     * @param request 编辑请求
     * @return 编辑结果
     */
    @PutMapping("/{id}")
    public ApiResponse<UpdateCourseResponse> updateCourse(@PathVariable Long id, @Valid @RequestBody UpdateCourseRequest request) {
        UserPrincipal currentUser = requireAdmin();
        UpdateCourseResult result = contentCourseService.updateCourse(new UpdateCourseCommand(
            id,
            request.title(),
            request.description(),
            request.coverImage(),
            request.status(),
            currentUser.userId()
        ));
        return ApiResponse.success(new UpdateCourseResponse(result.courseId(), result.status()));
    }

    /**
     * 软删除课程。
     *
     * @param id 课程 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<DeleteCourseResponse> deleteCourse(@PathVariable Long id) {
        UserPrincipal currentUser = requireAdmin();
        DeleteCourseResult result = contentCourseService.deleteCourse(new DeleteCourseCommand(id, currentUser.userId()));
        return ApiResponse.success(new DeleteCourseResponse(result.courseId(), result.status()));
    }

    /**
     * 编辑课程章节。
     *
     * @param id 课程 ID
     * @param chapterId 章节 ID
     * @param request 编辑请求
     * @return 编辑结果
     */
    @PutMapping("/{id}/chapters/{chapterId}")
    public ApiResponse<UpdateCourseChapterResponse> updateChapter(
        @PathVariable Long id,
        @PathVariable Long chapterId,
        @Valid @RequestBody UpdateCourseChapterRequest request
    ) {
        UserPrincipal currentUser = requireAdmin();
        UpdateCourseChapterResult result = contentCourseService.updateCourseChapter(new UpdateCourseChapterCommand(
            id,
            chapterId,
            request.title(),
            request.content(),
            request.sortOrder(),
            currentUser.userId()
        ));
        return ApiResponse.success(new UpdateCourseChapterResponse(result.courseId(), result.chapterId(), result.sortOrder()));
    }

    private UserPrincipal requireAdmin() {
        UserPrincipal currentUser = currentUserContext.currentUser()
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication is required."));
        if (currentUser.role() != UserRole.ADMIN.getCode()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Admin permission is required.");
        }
        return currentUser;
    }
}
