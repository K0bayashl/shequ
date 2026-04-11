package com.community.mvp.backend.application.content.service;

import com.community.mvp.backend.application.content.command.CreateCourseChapterCommand;
import com.community.mvp.backend.application.content.command.CreateCourseCommand;
import com.community.mvp.backend.application.content.query.GetChapterContentQuery;
import com.community.mvp.backend.application.content.query.GetCourseDetailQuery;
import com.community.mvp.backend.application.content.query.ListPublishedCoursesQuery;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.domain.content.model.Course;
import com.community.mvp.backend.domain.content.model.CourseChapter;
import com.community.mvp.backend.domain.content.model.CourseStatus;
import com.community.mvp.backend.domain.content.repository.CourseChapterRepository;
import com.community.mvp.backend.domain.content.repository.CourseRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class ContentCourseService {

    private final CourseRepository courseRepository;
    private final CourseChapterRepository chapterRepository;

    public ContentCourseService(CourseRepository courseRepository, CourseChapterRepository chapterRepository) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional
    public CreateCourseResult createCourse(CreateCourseCommand command) {
        String title = normalizeRequired(command.title(), "title");
        String description = normalizeRequired(command.description(), "description");
        String coverImage = normalizeOptional(command.coverImage());
        CourseStatus status = toCourseStatus(command.status());
        Long creatorUserId = requireUserId(command.creatorUserId());
        List<CreateCourseChapterCommand> chapterCommands = command.chapters() == null ? List.of() : command.chapters();

        assertDistinctSortOrder(chapterCommands);

        Course savedCourse = courseRepository.saveAndFlush(new Course(
            null,
            title,
            description,
            coverImage,
            status,
            0,
            null,
            null,
            null,
            creatorUserId,
            null,
            null
        ));

        for (CreateCourseChapterCommand chapterCommand : chapterCommands) {
            chapterRepository.saveAndFlush(new CourseChapter(
                null,
                savedCourse.id(),
                normalizeRequired(chapterCommand.title(), "chapter.title"),
                normalizeRequired(chapterCommand.content(), "chapter.content"),
                chapterCommand.sortOrder(),
                0,
                null,
                null,
                null,
                null,
                null
            ));
        }

        return new CreateCourseResult(savedCourse.id(), savedCourse.status().getCode(), chapterCommands.size());
    }

    @Transactional(readOnly = true)
    public List<CourseListItem> listPublishedCourses(ListPublishedCoursesQuery query) {
        return courseRepository.findAllByStatusOrderByUpdatedAtDesc(CourseStatus.PUBLISHED).stream()
            .map(course -> new CourseListItem(
                course.id(),
                course.title(),
                course.description(),
                course.coverImage(),
                Math.toIntExact(chapterRepository.countByCourseId(course.id())),
                course.updatedAt()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResult getCourseDetail(GetCourseDetailQuery query) {
        Course course = findPublishedCourseOrThrow(query.courseId());
        List<CourseChapterItem> chapters = chapterRepository.findAllByCourseIdOrderBySortOrderAsc(course.id()).stream()
            .map(chapter -> new CourseChapterItem(chapter.id(), chapter.title(), chapter.sortOrder()))
            .toList();

        return new CourseDetailResult(
            course.id(),
            course.title(),
            course.description(),
            course.coverImage(),
            chapters
        );
    }

    @Transactional(readOnly = true)
    public ChapterContentResult getChapterContent(GetChapterContentQuery query) {
        Course course = findPublishedCourseOrThrow(query.courseId());
        CourseChapter chapter = chapterRepository.findById(query.chapterId())
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Chapter not found."));

        if (!chapter.courseId().equals(course.id())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chapter does not belong to current course.");
        }

        return new ChapterContentResult(
            course.id(),
            chapter.id(),
            chapter.title(),
            chapter.sortOrder(),
            chapter.content()
        );
    }

    private Course findPublishedCourseOrThrow(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Course not found."));
        if (!course.status().isPublished()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Course is not available.");
        }
        return course;
    }

    private void assertDistinctSortOrder(List<CreateCourseChapterCommand> chapterCommands) {
        Set<Integer> seenSortOrders = new HashSet<>();
        for (CreateCourseChapterCommand chapter : chapterCommands) {
            if (chapter.sortOrder() <= 0) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "chapter.sortOrder must be greater than 0.");
            }
            if (!seenSortOrders.add(chapter.sortOrder())) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chapter sortOrder must be unique in one course.");
            }
        }
    }

    private Long requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication is required.");
        }
        return userId;
    }

    private CourseStatus toCourseStatus(int statusCode) {
        try {
            return CourseStatus.fromCode(statusCode);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid course status.");
        }
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
}
