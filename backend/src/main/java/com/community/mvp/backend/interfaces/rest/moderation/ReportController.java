package com.community.mvp.backend.interfaces.rest.moderation;

import com.community.mvp.backend.application.moderation.command.SubmitCourseReportCommand;
import com.community.mvp.backend.application.moderation.service.ContentModerationService;
import com.community.mvp.backend.application.moderation.service.SubmitCourseReportResult;
import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.infrastructure.security.CurrentUserContext;
import com.community.mvp.backend.interfaces.rest.moderation.dto.SubmitCourseReportRequest;
import com.community.mvp.backend.interfaces.rest.moderation.dto.SubmitCourseReportResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报提交接口。
 * <p>当前切片支持用户提交课程举报。</p>
 */
@RestController
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
@RequestMapping("/api/reports")
public class ReportController {

    private final ContentModerationService contentModerationService;
    private final CurrentUserContext currentUserContext;

    /**
     * 构造举报控制器。
     *
     * @param contentModerationService 内容治理服务
     * @param currentUserContext 当前用户上下文
     */
    public ReportController(ContentModerationService contentModerationService, CurrentUserContext currentUserContext) {
        this.contentModerationService = contentModerationService;
        this.currentUserContext = currentUserContext;
    }

    /**
     * 提交课程举报。
     *
     * @param request 举报请求
     * @return 举报结果
     */
    @PostMapping
    public ApiResponse<SubmitCourseReportResponse> submitCourseReport(@Valid @RequestBody SubmitCourseReportRequest request) {
        Long reporterUserId = currentUserContext.currentUser()
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication is required."))
            .userId();

        SubmitCourseReportResult result = contentModerationService.submitCourseReport(new SubmitCourseReportCommand(
            request.courseId(),
            request.reasonCode(),
            request.reasonDetail(),
            reporterUserId
        ));

        return ApiResponse.success(new SubmitCourseReportResponse(result.reportId(), result.status()));
    }
}
