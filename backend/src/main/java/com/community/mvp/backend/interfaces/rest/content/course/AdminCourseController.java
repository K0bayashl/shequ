package com.community.mvp.backend.interfaces.rest.content.course;

import com.community.mvp.backend.application.content.command.CreateCourseChapterCommand;
import com.community.mvp.backend.application.content.command.CreateCourseCommand;
import com.community.mvp.backend.application.content.service.ContentCourseService;
import com.community.mvp.backend.application.content.service.CreateCourseResult;
import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.infrastructure.security.CurrentUserContext;
import com.community.mvp.backend.interfaces.rest.content.course.dto.CreateCourseRequest;
import com.community.mvp.backend.interfaces.rest.content.course.dto.CreateCourseResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端课程维护接口。
 * <p>当前提供课程创建能力，要求管理员身份。</p>
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
        UserPrincipal currentUser = currentUserContext.currentUser()
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication is required."));
        if (currentUser.role() != UserRole.ADMIN.getCode()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Admin permission is required.");
        }

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
}
