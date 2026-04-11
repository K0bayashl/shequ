package com.community.mvp.backend.interfaces.rest.content.course;

import com.community.mvp.backend.application.content.query.GetChapterContentQuery;
import com.community.mvp.backend.application.content.query.GetCourseDetailQuery;
import com.community.mvp.backend.application.content.query.ListPublishedCoursesQuery;
import com.community.mvp.backend.application.content.service.ChapterContentResult;
import com.community.mvp.backend.application.content.service.ContentCourseService;
import com.community.mvp.backend.application.content.service.CourseDetailResult;
import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.interfaces.rest.content.course.dto.ChapterContentResponse;
import com.community.mvp.backend.interfaces.rest.content.course.dto.CourseChapterItemResponse;
import com.community.mvp.backend.interfaces.rest.content.course.dto.CourseDetailResponse;
import com.community.mvp.backend.interfaces.rest.content.course.dto.CourseListItemResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程内容对外查询接口。
 * <p>提供会员侧课程列表、课程详情与章节正文读取能力。</p>
 */
@RestController
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
@RequestMapping("/api/courses")
public class CourseController {

    private final ContentCourseService contentCourseService;

    /**
     * 构造课程查询控制器。
     *
     * @param contentCourseService 课程应用服务
     */
    public CourseController(ContentCourseService contentCourseService) {
        this.contentCourseService = contentCourseService;
    }

    /**
     * 查询已发布课程列表。
     *
     * @return 已发布课程的简要信息集合
     */
    @GetMapping
    public ApiResponse<List<CourseListItemResponse>> listCourses() {
        List<CourseListItemResponse> data = contentCourseService.listPublishedCourses(new ListPublishedCoursesQuery()).stream()
            .map(item -> new CourseListItemResponse(
                item.id(),
                item.title(),
                item.description(),
                item.coverImage(),
                item.chapterCount(),
                item.publishedAt()
            ))
            .toList();
        return ApiResponse.success(data);
    }

    /**
     * 查询课程详情（包含章节目录）。
     *
     * @param id 课程 ID
     * @return 课程详情及章节列表
     */
    @GetMapping("/{id}")
    public ApiResponse<CourseDetailResponse> getCourseDetail(@PathVariable Long id) {
        CourseDetailResult detail = contentCourseService.getCourseDetail(new GetCourseDetailQuery(id));
        return ApiResponse.success(new CourseDetailResponse(
            detail.id(),
            detail.title(),
            detail.description(),
            detail.coverImage(),
            detail.chapters().stream()
                .map(chapter -> new CourseChapterItemResponse(chapter.id(), chapter.title(), chapter.sortOrder()))
                .toList()
        ));
    }

    /**
     * 查询指定课程章节的正文内容。
     *
     * @param id 课程 ID
     * @param chapterId 章节 ID
     * @return 章节正文与顺序信息
     */
    @GetMapping("/{id}/chapters/{chapterId}")
    public ApiResponse<ChapterContentResponse> getChapterContent(@PathVariable Long id, @PathVariable Long chapterId) {
        ChapterContentResult result = contentCourseService.getChapterContent(new GetChapterContentQuery(id, chapterId));
        return ApiResponse.success(new ChapterContentResponse(
            result.courseId(),
            result.chapterId(),
            result.title(),
            result.sortOrder(),
            result.content()
        ));
    }
}
