package com.community.mvp.backend.interfaces.rest.moderation;

import com.community.mvp.backend.application.moderation.command.CourseModerationCommand;
import com.community.mvp.backend.application.moderation.command.HandleCourseReportCommand;
import com.community.mvp.backend.application.moderation.command.UserModerationCommand;
import com.community.mvp.backend.application.moderation.query.ListReportsQuery;
import com.community.mvp.backend.application.moderation.service.ContentModerationService;
import com.community.mvp.backend.application.moderation.service.CourseModerationResult;
import com.community.mvp.backend.application.moderation.service.HandleCourseReportResult;
import com.community.mvp.backend.application.moderation.service.ModerationReportItem;
import com.community.mvp.backend.application.moderation.service.UserModerationResult;
import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.infrastructure.security.CurrentUserContext;
import com.community.mvp.backend.interfaces.rest.moderation.dto.CourseModerationRequest;
import com.community.mvp.backend.interfaces.rest.moderation.dto.CourseModerationResponse;
import com.community.mvp.backend.interfaces.rest.moderation.dto.HandleCourseReportRequest;
import com.community.mvp.backend.interfaces.rest.moderation.dto.HandleCourseReportResponse;
import com.community.mvp.backend.interfaces.rest.moderation.dto.ModerationReportResponse;
import com.community.mvp.backend.interfaces.rest.moderation.dto.UserModerationRequest;
import com.community.mvp.backend.interfaces.rest.moderation.dto.UserModerationResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端治理接口。
 * <p>提供举报处理、课程上下架与用户封禁操作。</p>
 */
@RestController
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
@RequestMapping("/api/admin/moderation")
public class AdminModerationController {

    private final ContentModerationService contentModerationService;
    private final CurrentUserContext currentUserContext;

    /**
     * 构造管理端治理控制器。
     *
     * @param contentModerationService 内容治理服务
     * @param currentUserContext 当前用户上下文
     */
    public AdminModerationController(
        ContentModerationService contentModerationService,
        CurrentUserContext currentUserContext
    ) {
        this.contentModerationService = contentModerationService;
        this.currentUserContext = currentUserContext;
    }

    /**
     * 查询举报列表。
     *
     * @param status 举报状态筛选（all/pending/resolved/rejected）
     * @return 举报列表
     */
    @GetMapping("/reports")
    public ApiResponse<List<ModerationReportResponse>> listReports(@RequestParam(defaultValue = "all") String status) {
        requireAdmin();
        List<ModerationReportResponse> data = contentModerationService.listReports(new ListReportsQuery(status)).stream()
            .map(this::toResponse)
            .toList();
        return ApiResponse.success(data);
    }

    /**
     * 处理课程举报。
     *
     * @param reportId 举报 ID
     * @param request 处理请求
     * @return 处理结果
     */
    @PostMapping("/reports/{reportId}/handle")
    public ApiResponse<HandleCourseReportResponse> handleCourseReport(
        @PathVariable Long reportId,
        @Valid @RequestBody HandleCourseReportRequest request
    ) {
        UserPrincipal admin = requireAdmin();
        HandleCourseReportResult result = contentModerationService.handleCourseReport(new HandleCourseReportCommand(
            reportId,
            request.decision(),
            request.handleNote(),
            request.takedownCourse(),
            request.banAuthor(),
            admin.userId()
        ));
        return ApiResponse.success(new HandleCourseReportResponse(
            result.reportId(),
            result.status(),
            result.courseTakenDown(),
            result.authorBanned()
        ));
    }

    /**
     * 下架课程。
     *
     * @param courseId 课程 ID
     * @param request 下架请求
     * @return 课程状态
     */
    @PostMapping("/courses/{courseId}/takedown")
    public ApiResponse<CourseModerationResponse> takedownCourse(
        @PathVariable Long courseId,
        @RequestBody(required = false) CourseModerationRequest request
    ) {
        UserPrincipal admin = requireAdmin();
        String reason = request == null ? null : request.reason();
        CourseModerationResult result = contentModerationService.takeDownCourse(new CourseModerationCommand(
            courseId,
            reason,
            admin.userId()
        ));
        return ApiResponse.success(new CourseModerationResponse(result.courseId(), result.status()));
    }

    /**
     * 恢复课程。
     *
     * @param courseId 课程 ID
     * @param request 恢复请求
     * @return 课程状态
     */
    @PostMapping("/courses/{courseId}/restore")
    public ApiResponse<CourseModerationResponse> restoreCourse(
        @PathVariable Long courseId,
        @RequestBody(required = false) CourseModerationRequest request
    ) {
        UserPrincipal admin = requireAdmin();
        String reason = request == null ? null : request.reason();
        CourseModerationResult result = contentModerationService.restoreCourse(new CourseModerationCommand(
            courseId,
            reason,
            admin.userId()
        ));
        return ApiResponse.success(new CourseModerationResponse(result.courseId(), result.status()));
    }

    /**
     * 封禁用户。
     *
     * @param userId 用户 ID
     * @param request 封禁请求
     * @return 用户状态
     */
    @PostMapping("/users/{userId}/ban")
    public ApiResponse<UserModerationResponse> banUser(
        @PathVariable Long userId,
        @RequestBody(required = false) UserModerationRequest request
    ) {
        UserPrincipal admin = requireAdmin();
        String reason = request == null ? null : request.reason();
        UserModerationResult result = contentModerationService.banUser(new UserModerationCommand(
            userId,
            reason,
            admin.userId()
        ));
        return ApiResponse.success(new UserModerationResponse(result.userId(), result.status()));
    }

    /**
     * 解封用户。
     *
     * @param userId 用户 ID
     * @param request 解封请求
     * @return 用户状态
     */
    @PostMapping("/users/{userId}/unban")
    public ApiResponse<UserModerationResponse> unbanUser(
        @PathVariable Long userId,
        @RequestBody(required = false) UserModerationRequest request
    ) {
        UserPrincipal admin = requireAdmin();
        String reason = request == null ? null : request.reason();
        UserModerationResult result = contentModerationService.unbanUser(new UserModerationCommand(
            userId,
            reason,
            admin.userId()
        ));
        return ApiResponse.success(new UserModerationResponse(result.userId(), result.status()));
    }

    private ModerationReportResponse toResponse(ModerationReportItem item) {
        return new ModerationReportResponse(
            item.reportId(),
            item.contentType(),
            item.contentId(),
            item.reporterUserId(),
            item.reasonCode(),
            item.reasonDetail(),
            item.status(),
            item.handledBy(),
            item.handledAt(),
            item.handleNote(),
            item.createdAt()
        );
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
