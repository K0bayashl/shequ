"use client"

import { useEffect, useState } from "react"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  MessageSquare,
  TrendingUp,
  Clock,
  Star,
  ChevronDown,
  PenSquare,
  BookOpen,
  User,
  Flame,
  Award,
  Menu,
  X,
} from "lucide-react"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { getCommunityFeed, type TopicStat } from "@/lib/backend-api"

type ThreadType = "official" | "user"

interface Thread {
  id: string
  title: string
  author: {
    name: string
    avatar: string
    initials: string
  }
  type: ThreadType
  timeAgo: string
  commentCount: number
  tags: string[]
  isPinned?: boolean
}

const threads: Thread[] = [
  {
    id: "1",
    title: "yali 2.0 正式发布：全新架构与性能优化",
    author: { name: "yali Official", avatar: "", initials: "YO" },
    type: "official",
    timeAgo: "2 小时前",
    commentCount: 128,
    tags: ["公告", "更新"],
    isPinned: true,
  },
  {
    id: "2",
    title: "深入理解 React Server Components 的工作原理",
    author: { name: "yali Official", avatar: "", initials: "YO" },
    type: "official",
    timeAgo: "1 天前",
    commentCount: 89,
    tags: ["教程", "React"],
  },
  {
    id: "3",
    title: "分享：我如何用 Next.js 构建了一个日活 10 万的 SaaS 产品",
    author: { name: "张明远", avatar: "", initials: "ZM" },
    type: "user",
    timeAgo: "3 小时前",
    commentCount: 67,
    tags: ["经验分享", "Next.js"],
  },
  {
    id: "4",
    title: "TypeScript 5.4 新特性解析与最佳实践",
    author: { name: "yali Official", avatar: "", initials: "YO" },
    type: "official",
    timeAgo: "2 天前",
    commentCount: 45,
    tags: ["教程", "TypeScript"],
  },
  {
    id: "5",
    title: "求助：如何优雅地处理 React 中的复杂表单状态？",
    author: { name: "李小龙", avatar: "", initials: "LX" },
    type: "user",
    timeAgo: "5 小时前",
    commentCount: 23,
    tags: ["问答", "React"],
  },
  {
    id: "6",
    title: "开源项目推荐：一个轻量级的状态管理方案",
    author: { name: "王大锤", avatar: "", initials: "WD" },
    type: "user",
    timeAgo: "8 小时前",
    commentCount: 34,
    tags: ["开源", "工具"],
  },
]

const trendingTopics = [
  { name: "React 19", count: 234 },
  { name: "Next.js 15", count: 189 },
  { name: "TypeScript", count: 156 },
  { name: "AI 编程", count: 145 },
  { name: "Rust", count: 98 },
]

type FilterType = "all" | "official" | "user"
type SortType = "latest" | "hot" | "top"

function ThreadCard({ thread }: { thread: Thread }) {
  const isOfficial = thread.type === "official"

  return (
    <div
      className={`group relative border-b border-border p-4 transition-colors hover:bg-muted/50 ${
        thread.isPinned ? "bg-accent/5" : ""
      }`}
    >
      {thread.isPinned && <div className="absolute left-0 top-0 h-full w-0.5 bg-accent" />}
      <div className="flex gap-3">
        <Avatar
          className={`h-10 w-10 shrink-0 ${isOfficial ? "ring-2 ring-accent ring-offset-2 ring-offset-background" : ""}`}
        >
          <AvatarImage src={thread.author.avatar} alt={thread.author.name} />
          <AvatarFallback
            className={isOfficial ? "bg-accent text-accent-foreground" : "bg-muted text-muted-foreground"}
          >
            {thread.author.initials}
          </AvatarFallback>
        </Avatar>
        <div className="min-w-0 flex-1">
          <div className="mb-1.5 flex flex-wrap items-center gap-2">
            {isOfficial && (
              <Badge className="gap-1 border-0 bg-accent/10 text-xs font-medium text-accent hover:bg-accent/20">
                <BookOpen className="h-3 w-3" />
                官方文章
              </Badge>
            )}
            {thread.isPinned && (
              <Badge variant="outline" className="gap-1 border-muted-foreground/30 text-xs text-muted-foreground">
                <Star className="h-3 w-3" />
                置顶
              </Badge>
            )}
          </div>
          <h3 className="mb-2 cursor-pointer text-base font-medium leading-snug text-foreground transition-colors group-hover:text-accent">
            {thread.title}
          </h3>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-muted-foreground">
            <span className="flex items-center gap-1">
              <span className={`font-medium ${isOfficial ? "text-accent" : "text-foreground"}`}>
                {thread.author.name}
              </span>
            </span>
            <span className="flex items-center gap-1">
              <Clock className="h-3.5 w-3.5" />
              {thread.timeAgo}
            </span>
            <span className="flex items-center gap-1">
              <MessageSquare className="h-3.5 w-3.5" />
              {thread.commentCount} 条评论
            </span>
          </div>
          <div className="mt-2 flex flex-wrap gap-1.5">
            {thread.tags.map((tag) => (
              <Badge
                key={tag}
                variant="secondary"
                className="bg-muted text-xs font-normal text-muted-foreground hover:bg-muted/80"
              >
                {tag}
              </Badge>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

function RightSidebar({ topics }: { topics: TopicStat[] }) {
  return (
    <div className="space-y-4">
      {/* New Post CTA */}
      <Button className="w-full gap-2 bg-accent text-accent-foreground hover:bg-accent/90" size="lg">
        <PenSquare className="h-4 w-4" />
        发布新帖
      </Button>

      {/* Profile Card */}
      <Card className="border-border bg-card">
        <CardContent className="p-4">
          <div className="flex items-center gap-3">
            <Avatar className="h-12 w-12 ring-2 ring-accent/20">
              <AvatarFallback className="bg-muted text-foreground">YU</AvatarFallback>
            </Avatar>
            <div className="min-w-0 flex-1">
              <p className="truncate font-medium text-foreground">yali_user</p>
              <div className="mt-0.5 flex items-center gap-1.5">
                <Badge className="gap-1 border-0 bg-gradient-to-r from-accent to-accent/80 text-xs text-accent-foreground">
                  <Award className="h-3 w-3" />
                  Pro 会员
                </Badge>
              </div>
            </div>
          </div>
          <div className="mt-4 grid grid-cols-3 gap-2 text-center">
            <div className="rounded-md bg-muted/50 p-2">
              <p className="text-lg font-semibold text-foreground">23</p>
              <p className="text-xs text-muted-foreground">帖子</p>
            </div>
            <div className="rounded-md bg-muted/50 p-2">
              <p className="text-lg font-semibold text-foreground">156</p>
              <p className="text-xs text-muted-foreground">评论</p>
            </div>
            <div className="rounded-md bg-muted/50 p-2">
              <p className="text-lg font-semibold text-foreground">89</p>
              <p className="text-xs text-muted-foreground">获赞</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Trending Topics */}
      <Card className="border-border bg-card">
        <CardHeader className="pb-3">
          <CardTitle className="flex items-center gap-2 text-base font-medium text-foreground">
            <Flame className="h-4 w-4 text-accent" />
            热门话题
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <div className="space-y-2">
            {topics.map((topic, index) => (
              <div
                key={topic.name}
                className="flex cursor-pointer items-center justify-between rounded-md px-2 py-1.5 transition-colors hover:bg-muted"
              >
                <div className="flex items-center gap-2">
                  <span className="w-5 text-sm font-medium text-muted-foreground">{index + 1}</span>
                  <span className="text-sm text-foreground">{topic.name}</span>
                </div>
                <span className="text-xs text-muted-foreground">{topic.count} 讨论</span>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Quick Links */}
      <Card className="border-border bg-card">
        <CardContent className="p-4">
          <div className="space-y-1">
            <button className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground">
              <BookOpen className="h-4 w-4" />
              文档中心
            </button>
            <button className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground">
              <Star className="h-4 w-4" />
              我的收藏
            </button>
            <button className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground">
              <TrendingUp className="h-4 w-4" />
              排行榜
            </button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

export function CommunityView() {
  const [filter, setFilter] = useState<FilterType>("all")
  const [sort, setSort] = useState<SortType>("latest")
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [remoteThreads, setRemoteThreads] = useState<Thread[]>(threads)
  const [remoteTopics, setRemoteTopics] = useState<TopicStat[]>(trendingTopics)
  const [isSyncing, setIsSyncing] = useState(false)
  const [syncError, setSyncError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true

    const loadCommunityFeed = async () => {
      setIsSyncing(true)
      setSyncError(null)

      try {
        const response = await getCommunityFeed(filter, sort)
        if (!mounted) {
          return
        }
        setRemoteThreads(response.threads)
        setRemoteTopics(response.topics)
      } catch (error) {
        if (!mounted) {
          return
        }
        const message = error instanceof Error ? error.message : "社区数据同步失败"
        setSyncError(message)
      } finally {
        if (mounted) {
          setIsSyncing(false)
        }
      }
    }

    loadCommunityFeed()

    return () => {
      mounted = false
    }
  }, [filter, sort])

  const filteredThreads = remoteThreads.filter((thread) => {
    if (filter === "all") return true
    return thread.type === filter
  })

  const sortedThreads = [...filteredThreads].sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1
    if (!a.isPinned && b.isPinned) return 1
    if (sort === "hot") return b.commentCount - a.commentCount
    return 0
  })

  return (
    <div className="min-h-screen bg-background">
      {/* Main Content */}
      <main className="mx-auto max-w-7xl px-4 py-6">
        <div className="flex gap-6">
          {/* Left Column - Thread List */}
          <div className="min-w-0 flex-1 lg:flex-[7]">
            {/* Filters */}
            <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                <Button
                  variant={filter === "all" ? "default" : "outline"}
                  size="sm"
                  onClick={() => setFilter("all")}
                  className={filter === "all" ? "bg-foreground text-background hover:bg-foreground/90" : ""}
                >
                  全部
                </Button>
                <Button
                  variant={filter === "official" ? "default" : "outline"}
                  size="sm"
                  onClick={() => setFilter("official")}
                  className={filter === "official" ? "bg-accent text-accent-foreground hover:bg-accent/90" : ""}
                >
                  <BookOpen className="mr-1.5 h-3.5 w-3.5" />
                  官方文章
                </Button>
                <Button
                  variant={filter === "user" ? "default" : "outline"}
                  size="sm"
                  onClick={() => setFilter("user")}
                  className={filter === "user" ? "bg-foreground text-background hover:bg-foreground/90" : ""}
                >
                  <User className="mr-1.5 h-3.5 w-3.5" />
                  用户帖子
                </Button>
              </div>

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" size="sm" className="gap-1.5">
                    {sort === "latest" && <Clock className="h-3.5 w-3.5" />}
                    {sort === "hot" && <Flame className="h-3.5 w-3.5" />}
                    {sort === "top" && <TrendingUp className="h-3.5 w-3.5" />}
                    {sort === "latest" ? "最新" : sort === "hot" ? "最热" : "最佳"}
                    <ChevronDown className="h-3.5 w-3.5" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem onClick={() => setSort("latest")} className="gap-2">
                    <Clock className="h-4 w-4" />
                    最新发布
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => setSort("hot")} className="gap-2">
                    <Flame className="h-4 w-4" />
                    最多评论
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => setSort("top")} className="gap-2">
                    <TrendingUp className="h-4 w-4" />
                    本周最佳
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            <div className="mb-3 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
              {isSyncing && <span>正在同步社区数据...</span>}
              {!isSyncing && !syncError && <span>已接入后端社区接口</span>}
              {!isSyncing && syncError && <span className="text-destructive">同步失败：{syncError}</span>}
            </div>

            {/* Thread List */}
            <Card className="overflow-hidden border-border bg-card">
              <div className="divide-y-0">
                {sortedThreads.map((thread) => (
                  <ThreadCard key={thread.id} thread={thread} />
                ))}
              </div>
            </Card>

            {/* Load More */}
            <div className="mt-4 text-center">
              <Button variant="outline" className="w-full sm:w-auto">
                加载更多
              </Button>
            </div>
          </div>

          {/* Right Sidebar */}
          <aside className="hidden w-72 shrink-0 lg:block lg:flex-[3]">
            <div className="sticky top-20">
              <RightSidebar topics={remoteTopics} />
            </div>
          </aside>

          {/* Mobile Sidebar */}
          <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
            <SheetTrigger asChild>
              <Button
                variant="outline"
                size="icon"
                className="fixed bottom-4 right-4 z-50 h-12 w-12 rounded-full shadow-lg lg:hidden"
              >
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="w-80 p-0">
              <div className="flex h-14 items-center justify-between border-b border-border px-4">
                <span className="font-semibold text-foreground">菜单</span>
                <Button variant="ghost" size="icon" onClick={() => setMobileMenuOpen(false)}>
                  <X className="h-5 w-5" />
                </Button>
              </div>
              <div className="p-4">
                <RightSidebar topics={remoteTopics} />
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </main>
    </div>
  )
}
