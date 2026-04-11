package com.community.mvp.backend.interfaces.rest.content;

import com.community.mvp.backend.application.content.service.ContentSyncQueryService;
import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.AdminCdkItemResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.AdminCdkOverviewResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.CommunityAuthorResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.CommunityFeedResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.CommunityThreadResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.DocsChapterItemResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.DocsChapterListResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.DocsChapterResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.TopicStatResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 内容同步查询接口。
 * <p>提供社区流、管理端 CDK 总览和文档章节列表查询。</p>
 */
@RestController
@RequestMapping("/api/v1/content")
public class ContentSyncController {

    private final ObjectProvider<ContentSyncQueryService> contentSyncQueryServiceProvider;

    /**
     * 构造内容同步控制器。
     *
     * @param contentSyncQueryServiceProvider 内容查询服务提供器
     */
    public ContentSyncController(ObjectProvider<ContentSyncQueryService> contentSyncQueryServiceProvider) {
        this.contentSyncQueryServiceProvider = contentSyncQueryServiceProvider;
    }

    /**
     * 查询社区动态列表。
     *
     * @param filter 过滤条件：all、official、user
     * @param sort 排序条件：latest、hot、top
     * @return 社区动态与热门话题
     */
    @GetMapping("/community/feed")
    public ApiResponse<CommunityFeedResponse> communityFeed(
        @RequestParam(defaultValue = "all") String filter,
        @RequestParam(defaultValue = "latest") String sort
    ) {
        List<CommunityThreadResponse> threads = buildCommunityThreads().stream()
            .filter(thread -> "all".equalsIgnoreCase(filter) || thread.type().equalsIgnoreCase(filter))
            .toList();

        List<CommunityThreadResponse> sortedThreads = switch (sort.toLowerCase(Locale.ROOT)) {
            case "hot" -> threads.stream()
                .sorted(Comparator.comparingInt(CommunityThreadResponse::commentCount).reversed())
                .toList();
            case "top" -> threads.stream()
                .sorted(Comparator
                    .comparing(CommunityThreadResponse::isPinned).reversed()
                    .thenComparing(CommunityThreadResponse::commentCount, Comparator.reverseOrder()))
                .toList();
            default -> threads;
        };

        return ApiResponse.success(new CommunityFeedResponse(sortedThreads, buildTrendingTopics()));
    }

    /**
     * 查询管理端 CDK 总览。
     *
     * @param search 搜索关键词
     * @param status 状态筛选：all、used、unused、expired
     * @return CDK 总览数据
     */
    @GetMapping("/admin/cdks")
    public ApiResponse<AdminCdkOverviewResponse> adminCdks(
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "all") String status
    ) {
        ContentSyncQueryService queryService = contentSyncQueryServiceProvider.getIfAvailable();
        if (queryService != null) {
            return ApiResponse.success(queryService.queryAdminCdks(search, status));
        }

        List<AdminCdkItemResponse> allCdks = buildAdminCdks();

        String searchKeyword = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        List<AdminCdkItemResponse> filteredCdks = allCdks.stream()
            .filter(cdk -> "all".equalsIgnoreCase(status) || cdk.status().equalsIgnoreCase(status))
            .filter(cdk -> searchKeyword.isBlank() || containsKeyword(cdk, searchKeyword))
            .toList();

        int totalUsers = 1247;
        int activeMembers = 892;
        int remainingCdks = (int) allCdks.stream().filter(cdk -> "unused".equalsIgnoreCase(cdk.status())).count();

        return ApiResponse.success(new AdminCdkOverviewResponse(
            totalUsers,
            activeMembers,
            remainingCdks,
            filteredCdks
        ));
    }

    /**
     * 查询文档章节目录。
     *
     * @return 文档章节列表
     */
    @GetMapping("/docs/chapters")
    public ApiResponse<DocsChapterListResponse> docsChapters() {
        return ApiResponse.success(new DocsChapterListResponse(buildDocsChapters()));
    }

    /**
     * 判断 CDK 条目是否包含关键词。
     *
     * @param cdk CDK 条目
     * @param keyword 关键词
     * @return 是否命中
     */
    private boolean containsKeyword(AdminCdkItemResponse cdk, String keyword) {
        return safeContains(cdk.key(), keyword)
            || safeContains(cdk.usedBy(), keyword)
            || safeContains(cdk.usedByEmail(), keyword);
    }

    /**
     * 安全包含判断，避免空指针。
     *
     * @param source 源字符串
     * @param keyword 关键词
     * @return 是否包含关键词
     */
    private boolean safeContains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    /**
     * 构建社区动态示例数据。
     *
     * @return 社区动态列表
     */
    private List<CommunityThreadResponse> buildCommunityThreads() {
        return List.of(
            new CommunityThreadResponse(
                "1",
                "yali 2.0 正式发布：全新架构与性能优化",
                new CommunityAuthorResponse("yali Official", "", "YO"),
                "official",
                "2 小时前",
                128,
                List.of("公告", "更新"),
                true
            ),
            new CommunityThreadResponse(
                "2",
                "深入理解 React Server Components 的工作原理",
                new CommunityAuthorResponse("yali Official", "", "YO"),
                "official",
                "1 天前",
                89,
                List.of("教程", "React"),
                false
            ),
            new CommunityThreadResponse(
                "3",
                "分享：我如何用 Next.js 构建了一个日活 10 万的 SaaS 产品",
                new CommunityAuthorResponse("张明远", "", "ZM"),
                "user",
                "3 小时前",
                67,
                List.of("经验分享", "Next.js"),
                false
            ),
            new CommunityThreadResponse(
                "4",
                "TypeScript 5.4 新特性解析与最佳实践",
                new CommunityAuthorResponse("yali Official", "", "YO"),
                "official",
                "2 天前",
                45,
                List.of("教程", "TypeScript"),
                false
            ),
            new CommunityThreadResponse(
                "5",
                "求助：如何优雅地处理 React 中的复杂表单状态？",
                new CommunityAuthorResponse("李小龙", "", "LX"),
                "user",
                "5 小时前",
                23,
                List.of("问答", "React"),
                false
            ),
            new CommunityThreadResponse(
                "6",
                "开源项目推荐：一个轻量级的状态管理方案",
                new CommunityAuthorResponse("王大锤", "", "WD"),
                "user",
                "8 小时前",
                34,
                List.of("开源", "工具"),
                false
            )
        );
    }

    /**
     * 构建热门话题示例数据。
     *
     * @return 热门话题列表
     */
    private List<TopicStatResponse> buildTrendingTopics() {
        return List.of(
            new TopicStatResponse("React 19", 234),
            new TopicStatResponse("Next.js 15", 189),
            new TopicStatResponse("TypeScript", 156),
            new TopicStatResponse("AI 编程", 145),
            new TopicStatResponse("Rust", 98)
        );
    }

    /**
     * 构建管理端 CDK 示例数据。
     *
     * @return CDK 条目列表
     */
    private List<AdminCdkItemResponse> buildAdminCdks() {
        return List.of(
            new AdminCdkItemResponse(1L, "YALI-2024-AXKJ-8F92", "used", "Chen Wei", "chen.wei@example.com", "2024-03-15", "2024-03-01"),
            new AdminCdkItemResponse(2L, "YALI-2024-BMNP-3D47", "used", "Li Ming", "li.ming@example.com", "2024-03-14", "2024-03-01"),
            new AdminCdkItemResponse(3L, "YALI-2024-CQRS-6H21", "unused", null, null, null, "2024-03-10"),
            new AdminCdkItemResponse(4L, "YALI-2024-DTUV-9K58", "unused", null, null, null, "2024-03-10"),
            new AdminCdkItemResponse(5L, "YALI-2024-EWXY-2L84", "used", "Wang Fang", "wang.fang@example.com", "2024-03-12", "2024-03-05"),
            new AdminCdkItemResponse(6L, "YALI-2024-FZAB-5M37", "expired", null, null, null, "2024-01-01"),
            new AdminCdkItemResponse(7L, "YALI-2024-GCDE-7N69", "unused", null, null, null, "2024-03-12"),
            new AdminCdkItemResponse(8L, "YALI-2024-HFGH-1P42", "used", "Zhang Lei", "zhang.lei@example.com", "2024-03-13", "2024-03-08")
        );
    }

    /**
     * 构建文档章节示例数据。
     *
     * @return 文档章节列表
     */
    private List<DocsChapterResponse> buildDocsChapters() {
        return List.of(
            new DocsChapterResponse("Getting Started", "book", List.of(
                new DocsChapterItemResponse("Introduction", "#", false),
                new DocsChapterItemResponse("Installation", "#", true),
                new DocsChapterItemResponse("Quick Start Guide", "#", false)
            )),
            new DocsChapterResponse("Core Concepts", "layers", List.of(
                new DocsChapterItemResponse("Architecture Overview", "#", false),
                new DocsChapterItemResponse("Data Flow", "#", false),
                new DocsChapterItemResponse("State Management", "#", false),
                new DocsChapterItemResponse("Error Handling", "#", false)
            )),
            new DocsChapterResponse("API Reference", "code", List.of(
                new DocsChapterItemResponse("Authentication", "#", false),
                new DocsChapterItemResponse("Endpoints", "#", false),
                new DocsChapterItemResponse("Webhooks", "#", false)
            )),
            new DocsChapterResponse("CLI Tools", "terminal", List.of(
                new DocsChapterItemResponse("Commands", "#", false),
                new DocsChapterItemResponse("Configuration", "#", false)
            )),
            new DocsChapterResponse("Advanced", "settings", List.of(
                new DocsChapterItemResponse("Custom Plugins", "#", false),
                new DocsChapterItemResponse("Performance", "#", false),
                new DocsChapterItemResponse("Security", "#", false)
            ))
        );
    }
}
