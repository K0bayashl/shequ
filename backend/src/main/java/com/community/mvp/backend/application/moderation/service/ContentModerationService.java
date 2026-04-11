package com.community.mvp.backend.application.moderation.service;

import com.community.mvp.backend.application.moderation.command.CourseModerationCommand;
import com.community.mvp.backend.application.moderation.command.HandleCourseReportCommand;
import com.community.mvp.backend.application.moderation.command.SubmitCourseReportCommand;
import com.community.mvp.backend.application.moderation.command.UserModerationCommand;
import com.community.mvp.backend.application.moderation.query.ListReportsQuery;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.domain.content.model.Course;
import com.community.mvp.backend.domain.content.model.CourseStatus;
import com.community.mvp.backend.domain.content.repository.CourseRepository;
import com.community.mvp.backend.domain.moderation.model.ActionAuditLog;
import com.community.mvp.backend.domain.moderation.model.ContentReport;
import com.community.mvp.backend.domain.moderation.model.ReportContentType;
import com.community.mvp.backend.domain.moderation.model.ReportStatus;
import com.community.mvp.backend.domain.moderation.repository.ActionAuditLogRepository;
import com.community.mvp.backend.domain.moderation.repository.ContentReportRepository;
import com.community.mvp.backend.domain.user.model.UserAccount;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.domain.user.repository.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 内容治理应用服务。
 * <p>提供举报、处理举报、课程下架/恢复、用户封禁/解封与审计留痕。</p>
 */
@Service
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class ContentModerationService {

    private static final String ACTION_REPORT_SUBMIT = "report_submit";
    private static final String ACTION_REPORT_HANDLE = "report_handle";
    private static final String ACTION_COURSE_TAKEDOWN = "course_takedown";
    private static final String ACTION_COURSE_RESTORE = "course_restore";
    private static final String ACTION_USER_BAN = "user_ban";
    private static final String ACTION_USER_UNBAN = "user_unban";

    private static final String ACTION_RESULT_SUCCESS = "success";
    private static final String TARGET_TYPE_COURSE = "course";
    private static final String TARGET_TYPE_USER = "user";
    private static final String TARGET_TYPE_REPORT = "report";

    private final ContentReportRepository contentReportRepository;
    private final ActionAuditLogRepository actionAuditLogRepository;
    private final CourseRepository courseRepository;
    private final UserAccountRepository userAccountRepository;

    /**
     * 构造内容治理应用服务。
     *
     * @param contentReportRepository 举报仓储
     * @param actionAuditLogRepository 审计仓储
     * @param courseRepository 课程仓储
     * @param userAccountRepository 用户仓储
     */
    public ContentModerationService(
        ContentReportRepository contentReportRepository,
        ActionAuditLogRepository actionAuditLogRepository,
        CourseRepository courseRepository,
        UserAccountRepository userAccountRepository
    ) {
        this.contentReportRepository = contentReportRepository;
        this.actionAuditLogRepository = actionAuditLogRepository;
        this.courseRepository = courseRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * 提交课程举报。
     *
     * @param command 举报命令
     * @return 举报提交结果
     */
    @Transactional
    public SubmitCourseReportResult submitCourseReport(SubmitCourseReportCommand command) {
        Long reporterUserId = requirePositive(command.reporterUserId(), "reporterUserId");
        Long courseId = requirePositive(command.courseId(), "courseId");
        String reasonCode = normalizeRequired(command.reasonCode(), "reasonCode");
        String reasonDetail = normalizeOptional(command.reasonDetail());

        courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Course not found."));

        ContentReport saved = contentReportRepository.saveAndFlush(new ContentReport(
            null,
            ReportContentType.COURSE,
            courseId,
            reporterUserId,
            reasonCode,
            reasonDetail,
            ReportStatus.PENDING,
            null,
            null,
            null,
            null,
            null
        ));

        writeAudit(
            reporterUserId,
            ACTION_REPORT_SUBMIT,
            TARGET_TYPE_COURSE,
            courseId,
            ACTION_RESULT_SUCCESS,
            "{\"reportId\":" + saved.id() + "}"
        );

        return new SubmitCourseReportResult(saved.id(), saved.status().getCode());
    }

    /**
     * 查询举报列表。
     *
     * @param query 查询条件
     * @return 举报列表
     */
    @Transactional(readOnly = true)
    public List<ModerationReportItem> listReports(ListReportsQuery query) {
        String statusKeyword = query.status();
        List<ContentReport> reports;
        if (!StringUtils.hasText(statusKeyword) || "all".equalsIgnoreCase(statusKeyword)) {
            reports = contentReportRepository.findAllByOrderByCreatedAtDesc();
        } else {
            ReportStatus status;
            try {
                status = ReportStatus.fromKeyword(statusKeyword.trim().toLowerCase());
            } catch (IllegalArgumentException exception) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid report status.");
            }
            reports = contentReportRepository.findAllByStatusOrderByCreatedAtDesc(status);
        }

        return reports.stream()
            .map(report -> new ModerationReportItem(
                report.id(),
                report.contentType().getCode(),
                report.contentId(),
                report.reporterUserId(),
                report.reasonCode(),
                report.reasonDetail(),
                report.status().getCode(),
                report.handledBy(),
                report.handledAt(),
                report.handleNote(),
                report.createdAt()
            ))
            .toList();
    }

    /**
     * 处理课程举报，可联动课程下架和作者封禁。
     *
     * @param command 处理命令
     * @return 处理结果
     */
    @Transactional
    public HandleCourseReportResult handleCourseReport(HandleCourseReportCommand command) {
        Long handlerUserId = requirePositive(command.handlerUserId(), "handlerUserId");
        Long reportId = requirePositive(command.reportId(), "reportId");
        String decision = normalizeRequired(command.decision(), "decision").toLowerCase();
        String handleNote = normalizeOptional(command.handleNote());

        ContentReport report = contentReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Report not found."));
        if (report.status() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Report has already been handled.");
        }
        if (report.contentType() != ReportContentType.COURSE) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Only course reports are supported in this slice.");
        }

        Course targetCourse = courseRepository.findById(report.contentId())
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Course not found."));

        boolean courseTakenDown = false;
        boolean authorBanned = false;
        ContentReport handledReport;
        LocalDateTime now = LocalDateTime.now();

        if ("approve".equals(decision)) {
            handledReport = report.resolve(handlerUserId, handleNote, now);
            if (command.takedownCourse()) {
                takeDownCourseInternal(targetCourse, handleNote, handlerUserId);
                courseTakenDown = true;
            }
            if (command.banAuthor()) {
                banUserInternal(targetCourse.createdBy(), handleNote, handlerUserId);
                authorBanned = true;
            }
        } else if ("reject".equals(decision)) {
            handledReport = report.reject(handlerUserId, handleNote, now);
        } else {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "decision must be approve or reject.");
        }

        ContentReport saved = contentReportRepository.saveAndFlush(handledReport);

        writeAudit(
            handlerUserId,
            ACTION_REPORT_HANDLE,
            TARGET_TYPE_REPORT,
            saved.id(),
            ACTION_RESULT_SUCCESS,
            "{\"decision\":\"" + decision + "\",\"courseTakenDown\":" + courseTakenDown
                + ",\"authorBanned\":" + authorBanned + "}"
        );

        return new HandleCourseReportResult(saved.id(), saved.status().getCode(), courseTakenDown, authorBanned);
    }

    /**
     * 下架课程。
     *
     * @param command 下架命令
     * @return 课程治理结果
     */
    @Transactional
    public CourseModerationResult takeDownCourse(CourseModerationCommand command) {
        Course course = findCourseOrThrow(command.courseId());
        Long handlerUserId = requirePositive(command.handlerUserId(), "handlerUserId");
        String reason = normalizeOptional(command.reason());

        Course updatedCourse = takeDownCourseInternal(course, reason, handlerUserId);
        return new CourseModerationResult(updatedCourse.id(), updatedCourse.status().getCode());
    }

    /**
     * 恢复课程为已发布状态。
     *
     * @param command 恢复命令
     * @return 课程治理结果
     */
    @Transactional
    public CourseModerationResult restoreCourse(CourseModerationCommand command) {
        Course course = findCourseOrThrow(command.courseId());
        Long handlerUserId = requirePositive(command.handlerUserId(), "handlerUserId");
        String reason = normalizeOptional(command.reason());

        Course restored = courseRepository.saveAndFlush(new Course(
            course.id(),
            course.title(),
            course.description(),
            course.coverImage(),
            CourseStatus.PUBLISHED,
            0,
            null,
            handlerUserId,
            LocalDateTime.now(),
            course.createdBy(),
            course.createdAt(),
            course.updatedAt()
        ));

        writeAudit(
            handlerUserId,
            ACTION_COURSE_RESTORE,
            TARGET_TYPE_COURSE,
            restored.id(),
            ACTION_RESULT_SUCCESS,
            "{\"reason\":\"" + safeJsonValue(reason) + "\"}"
        );

        return new CourseModerationResult(restored.id(), restored.status().getCode());
    }

    /**
     * 封禁用户。
     *
     * @param command 封禁命令
     * @return 用户治理结果
     */
    @Transactional
    public UserModerationResult banUser(UserModerationCommand command) {
        UserAccount user = findUserOrThrow(command.userId());
        Long handlerUserId = requirePositive(command.handlerUserId(), "handlerUserId");
        String reason = normalizeOptional(command.reason());
        UserAccount updated = banUserInternal(user.id(), reason, handlerUserId);
        return new UserModerationResult(updated.id(), updated.status().getCode());
    }

    /**
     * 解封用户。
     *
     * @param command 解封命令
     * @return 用户治理结果
     */
    @Transactional
    public UserModerationResult unbanUser(UserModerationCommand command) {
        UserAccount user = findUserOrThrow(command.userId());
        Long handlerUserId = requirePositive(command.handlerUserId(), "handlerUserId");
        String reason = normalizeOptional(command.reason());

        if (user.status() == UserStatus.ACTIVE) {
            return new UserModerationResult(user.id(), user.status().getCode());
        }

        UserAccount updated = userAccountRepository.saveAndFlush(user.withStatus(UserStatus.ACTIVE));

        writeAudit(
            handlerUserId,
            ACTION_USER_UNBAN,
            TARGET_TYPE_USER,
            updated.id(),
            ACTION_RESULT_SUCCESS,
            "{\"reason\":\"" + safeJsonValue(reason) + "\"}"
        );

        return new UserModerationResult(updated.id(), updated.status().getCode());
    }

    private Course findCourseOrThrow(Long courseId) {
        Long normalizedCourseId = requirePositive(courseId, "courseId");
        return courseRepository.findById(normalizedCourseId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Course not found."));
    }

    private UserAccount findUserOrThrow(Long userId) {
        Long normalizedUserId = requirePositive(userId, "userId");
        return userAccountRepository.findById(normalizedUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "User not found."));
    }

    private Course takeDownCourseInternal(Course course, String reason, Long handlerUserId) {
        Course updated = courseRepository.saveAndFlush(new Course(
            course.id(),
            course.title(),
            course.description(),
            course.coverImage(),
            CourseStatus.OFFLINE,
            1,
            reason,
            handlerUserId,
            LocalDateTime.now(),
            course.createdBy(),
            course.createdAt(),
            course.updatedAt()
        ));

        writeAudit(
            handlerUserId,
            ACTION_COURSE_TAKEDOWN,
            TARGET_TYPE_COURSE,
            updated.id(),
            ACTION_RESULT_SUCCESS,
            "{\"reason\":\"" + safeJsonValue(reason) + "\"}"
        );

        return updated;
    }

    private UserAccount banUserInternal(Long userId, String reason, Long handlerUserId) {
        UserAccount user = findUserOrThrow(userId);
        if (user.role() == UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Admin account cannot be banned.");
        }
        if (user.status() == UserStatus.DISABLED) {
            return user;
        }

        UserAccount updated = userAccountRepository.saveAndFlush(user.withStatus(UserStatus.DISABLED));

        writeAudit(
            handlerUserId,
            ACTION_USER_BAN,
            TARGET_TYPE_USER,
            updated.id(),
            ACTION_RESULT_SUCCESS,
            "{\"reason\":\"" + safeJsonValue(reason) + "\"}"
        );

        return updated;
    }

    private void writeAudit(
        Long actorUserId,
        String actionType,
        String targetType,
        Long targetId,
        String actionResult,
        String detailJson
    ) {
        actionAuditLogRepository.saveAndFlush(new ActionAuditLog(
            null,
            actorUserId,
            actionType,
            targetType,
            targetId,
            actionResult,
            detailJson,
            null
        ));
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " must be greater than 0.");
        }
        return value;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " must not be blank.");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String safeJsonValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
