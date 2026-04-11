package com.community.mvp.backend.application.content;

import com.community.mvp.backend.application.content.command.CreateCourseChapterCommand;
import com.community.mvp.backend.application.content.command.CreateCourseCommand;
import com.community.mvp.backend.application.content.command.DeleteCourseCommand;
import com.community.mvp.backend.application.content.command.UpdateCourseCommand;
import com.community.mvp.backend.application.content.query.GetChapterContentQuery;
import com.community.mvp.backend.application.content.query.GetCourseDetailQuery;
import com.community.mvp.backend.application.content.query.ListPublishedCoursesQuery;
import com.community.mvp.backend.application.content.service.ContentCourseService;
import com.community.mvp.backend.application.content.service.CourseDetailResult;
import com.community.mvp.backend.application.content.service.CreateCourseResult;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.infrastructure.persistence.content.repository.jpa.JpaCourseChapterRepository;
import com.community.mvp.backend.infrastructure.persistence.content.repository.jpa.JpaCourseRepository;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaCdkRepository;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaUserAccountRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ContentCourseServiceTests {

    @Autowired
    private ContentCourseService contentCourseService;

    @Autowired
    private JpaCourseChapterRepository chapterRepository;

    @Autowired
    private JpaCourseRepository courseRepository;

    @Autowired
    private JpaCdkRepository cdkRepository;

    @Autowired
    private JpaUserAccountRepository userAccountRepository;

    @BeforeEach
    void setUp() {
        chapterRepository.deleteAll();
        courseRepository.deleteAll();
        cdkRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void shouldReturnChaptersSortedBySortOrder() {
        CreateCourseResult createResult = contentCourseService.createCourse(new CreateCourseCommand(
            "内容服务课程",
            "描述",
            null,
            1,
            List.of(
                new CreateCourseChapterCommand("第二章", "c2", 2),
                new CreateCourseChapterCommand("第一章", "c1", 1)
            ),
            1L
        ));

        CourseDetailResult detail = contentCourseService.getCourseDetail(new GetCourseDetailQuery(createResult.courseId()));
        assertEquals(2, detail.chapters().size());
        assertEquals(1, detail.chapters().get(0).sortOrder());
        assertEquals(2, detail.chapters().get(1).sortOrder());
    }

    @Test
    void shouldRejectDuplicatedChapterSortOrderInOneCourse() {
        assertThrows(BusinessException.class, () ->
            contentCourseService.createCourse(new CreateCourseCommand(
                "重复排序课程",
                "描述",
                null,
                1,
                List.of(
                    new CreateCourseChapterCommand("章节A", "A", 1),
                    new CreateCourseChapterCommand("章节B", "B", 1)
                ),
                1L
            ))
        );
    }

    @Test
    void shouldRejectChapterWhenNotBelongingToCourse() {
        CreateCourseResult firstCourse = contentCourseService.createCourse(new CreateCourseCommand(
            "课程一",
            "描述",
            null,
            1,
            List.of(new CreateCourseChapterCommand("章节1", "正文", 1)),
            1L
        ));
        CreateCourseResult secondCourse = contentCourseService.createCourse(new CreateCourseCommand(
            "课程二",
            "描述",
            null,
            1,
            List.of(new CreateCourseChapterCommand("章节2", "正文", 1)),
            1L
        ));

        Long chapterIdOfFirstCourse = chapterRepository.findAllByCourseIdOrderBySortOrderAscIdAsc(firstCourse.courseId())
            .get(0)
            .getId();

        assertThrows(BusinessException.class, () ->
            contentCourseService.getChapterContent(new GetChapterContentQuery(secondCourse.courseId(), chapterIdOfFirstCourse))
        );
    }

    @Test
    void shouldUpdateCourseTitleAndDescription() {
        CreateCourseResult created = contentCourseService.createCourse(new CreateCourseCommand(
            "旧课程标题",
            "旧描述",
            null,
            1,
            List.of(new CreateCourseChapterCommand("章节1", "正文", 1)),
            1L
        ));

        contentCourseService.updateCourse(new UpdateCourseCommand(
            created.courseId(),
            "新课程标题",
            "新描述",
            "https://example.com/new-cover.png",
            1,
            1L
        ));

        CourseDetailResult detail = contentCourseService.getCourseDetail(new GetCourseDetailQuery(created.courseId()));
        assertEquals("新课程标题", detail.title());
        assertEquals("新描述", detail.description());
    }

    @Test
    void shouldSoftDeleteCourseAndHideFromPublishedList() {
        CreateCourseResult created = contentCourseService.createCourse(new CreateCourseCommand(
            "待删除课程",
            "描述",
            null,
            1,
            List.of(new CreateCourseChapterCommand("章节1", "正文", 1)),
            1L
        ));

        contentCourseService.deleteCourse(new DeleteCourseCommand(created.courseId(), 1L));

        assertEquals(0, contentCourseService.listPublishedCourses(new ListPublishedCoursesQuery()).size());
        assertThrows(BusinessException.class, () ->
            contentCourseService.getCourseDetail(new GetCourseDetailQuery(created.courseId()))
        );
    }
}
