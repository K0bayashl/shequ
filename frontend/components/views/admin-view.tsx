"use client"

import { useCallback, useEffect, useMemo, useState } from "react"
import {
  BookOpen,
  Check,
  Copy,
  Key,
  LayoutDashboard,
  Loader2,
  Menu,
  RefreshCw,
  Search,
  Shield,
  Users,
  X,
} from "lucide-react"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  createCourse,
  getAdminCdks,
  getCourses,
  type AdminCdkItem,
  type CourseListItem,
  type CreateCourseRequest,
} from "@/lib/backend-api"

const navItems = [
  { id: "overview", label: "Overview", icon: LayoutDashboard },
  { id: "users", label: "Users", icon: Users },
  { id: "courses", label: "Courses", icon: BookOpen },
  { id: "moderation", label: "Content Moderation", icon: Shield },
  { id: "cdk", label: "CDK Management", icon: Key },
]

const mockCDKs: AdminCdkItem[] = [
  {
    id: 1,
    key: "YALI-2024-AXKJ-8F92",
    status: "used",
    usedBy: "Chen Wei",
    usedByEmail: "chen.wei@example.com",
    usedAt: "2024-03-15",
    createdAt: "2024-03-01",
  },
]

export function AdminView() {
  const [activeNav, setActiveNav] = useState("courses")
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const [cdkRows, setCdkRows] = useState<AdminCdkItem[]>(mockCDKs)
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<"all" | "used" | "unused" | "expired">("all")
  const [isCdkSyncing, setIsCdkSyncing] = useState(false)
  const [cdkSyncError, setCdkSyncError] = useState<string | null>(null)
  const [copiedKey, setCopiedKey] = useState<string | null>(null)

  const [courses, setCourses] = useState<CourseListItem[]>([])
  const [isCoursesLoading, setIsCoursesLoading] = useState(false)
  const [coursesError, setCoursesError] = useState<string | null>(null)
  const [isCreatingCourse, setIsCreatingCourse] = useState(false)
  const [createCourseError, setCreateCourseError] = useState<string | null>(null)
  const [createCourseSuccess, setCreateCourseSuccess] = useState<string | null>(null)

  const [courseTitle, setCourseTitle] = useState("")
  const [courseDescription, setCourseDescription] = useState("")
  const [courseCoverImage, setCourseCoverImage] = useState("")
  const [courseStatus, setCourseStatus] = useState<"0" | "1" | "2">("1")
  const [chapterTitle, setChapterTitle] = useState("")
  const [chapterContent, setChapterContent] = useState("")
  const [chapterSortOrder, setChapterSortOrder] = useState("1")

  const activeNavMeta = useMemo(() => navItems.find((item) => item.id === activeNav), [activeNav])

  const syncAdminCdks = useCallback(async (search = searchQuery, status = statusFilter) => {
    setIsCdkSyncing(true)
    setCdkSyncError(null)

    try {
      const response = await getAdminCdks(search, status)
      setCdkRows(response.cdks)
    } catch (error) {
      const message = error instanceof Error ? error.message : "CDK 数据同步失败"
      setCdkSyncError(message)
    } finally {
      setIsCdkSyncing(false)
    }
  }, [searchQuery, statusFilter])

  const syncCourses = useCallback(async () => {
    setIsCoursesLoading(true)
    setCoursesError(null)

    try {
      const response = await getCourses()
      setCourses(response)
    } catch (error) {
      const message = error instanceof Error ? error.message : "课程列表同步失败"
      setCoursesError(message)
    } finally {
      setIsCoursesLoading(false)
    }
  }, [])

  useEffect(() => {
    if (activeNav === "cdk") {
      void syncAdminCdks()
    }
    if (activeNav === "courses") {
      void syncCourses()
    }
  }, [activeNav, syncAdminCdks, syncCourses])

  useEffect(() => {
    if (activeNav !== "cdk") {
      return
    }

    const timer = setTimeout(() => {
      void syncAdminCdks(searchQuery, statusFilter)
    }, 250)

    return () => {
      clearTimeout(timer)
    }
  }, [activeNav, searchQuery, statusFilter, syncAdminCdks])

  const copyToClipboard = (key: string) => {
    navigator.clipboard.writeText(key)
    setCopiedKey(key)
    setTimeout(() => setCopiedKey(null), 1500)
  }

  const handleCreateCourse = async () => {
    setCreateCourseError(null)
    setCreateCourseSuccess(null)

    if (!courseTitle.trim() || !courseDescription.trim() || !chapterTitle.trim() || !chapterContent.trim()) {
      setCreateCourseError("请先填写课程标题、描述与至少一个章节内容")
      return
    }

    const parsedSortOrder = Number(chapterSortOrder)
    if (!Number.isInteger(parsedSortOrder) || parsedSortOrder <= 0) {
      setCreateCourseError("章节排序号必须是大于 0 的整数")
      return
    }

    const payload: CreateCourseRequest = {
      title: courseTitle.trim(),
      description: courseDescription.trim(),
      coverImage: courseCoverImage.trim() || undefined,
      status: Number(courseStatus) as 0 | 1 | 2,
      chapters: [
        {
          title: chapterTitle.trim(),
          content: chapterContent,
          sortOrder: parsedSortOrder,
        },
      ],
    }

    setIsCreatingCourse(true)

    try {
      const response = await createCourse(payload)
      setCreateCourseSuccess(`课程已创建，ID=${response.courseId}`)
      setCourseTitle("")
      setCourseDescription("")
      setCourseCoverImage("")
      setCourseStatus("1")
      setChapterTitle("")
      setChapterContent("")
      setChapterSortOrder("1")
      await syncCourses()
    } catch (error) {
      const message = error instanceof Error ? error.message : "创建课程失败"
      setCreateCourseError(message)
    } finally {
      setIsCreatingCourse(false)
    }
  }

  return (
    <div className="flex min-h-screen bg-background">
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-foreground/20 backdrop-blur-sm lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-border bg-card transition-transform duration-200 lg:static lg:translate-x-0 ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex h-16 items-center justify-between border-b border-border px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent">
              <Key className="h-4 w-4 text-accent-foreground" />
            </div>
            <span className="text-lg font-semibold text-foreground">yali Admin</span>
          </div>
          <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setSidebarOpen(false)}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        <nav className="flex-1 space-y-1 p-4">
          {navItems.map((item) => {
            const Icon = item.icon
            const isActive = activeNav === item.id
            return (
              <button
                key={item.id}
                onClick={() => setActiveNav(item.id)}
                className={`flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-accent text-accent-foreground"
                    : "text-muted-foreground hover:bg-secondary hover:text-foreground"
                }`}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </button>
            )
          })}
        </nav>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border bg-card/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-card/60 lg:px-8">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setSidebarOpen(true)}>
              <Menu className="h-5 w-5" />
            </Button>
            <div>
              <h1 className="text-lg font-semibold text-foreground">{activeNavMeta?.label ?? "Admin"}</h1>
              <p className="text-sm text-muted-foreground">
                {activeNav === "courses"
                  ? "同步课程接口并发布课程内容"
                  : activeNav === "cdk"
                    ? "管理 CDK 激活码"
                    : "该模块尚未在当前切片实现"}
              </p>
            </div>
          </div>

          <Button
            variant="outline"
            size="sm"
            onClick={() => void (activeNav === "courses" ? syncCourses() : syncAdminCdks())}
          >
            <RefreshCw className="mr-2 h-4 w-4" />
            刷新
          </Button>
        </header>

        <main className="flex-1 space-y-6 p-4 lg:p-8">
          {activeNav === "courses" && (
            <>
              <Card className="border-border">
                <CardHeader>
                  <CardTitle>创建课程（管理员）</CardTitle>
                </CardHeader>
                <CardContent className="grid gap-4">
                  {createCourseError && <p className="text-sm text-destructive">{createCourseError}</p>}
                  {createCourseSuccess && <p className="text-sm text-green-600">{createCourseSuccess}</p>}

                  <div className="grid gap-2">
                    <Label htmlFor="course-title">课程标题</Label>
                    <Input id="course-title" value={courseTitle} onChange={(e) => setCourseTitle(e.target.value)} />
                  </div>

                  <div className="grid gap-2">
                    <Label htmlFor="course-description">课程简介</Label>
                    <Input
                      id="course-description"
                      value={courseDescription}
                      onChange={(e) => setCourseDescription(e.target.value)}
                    />
                  </div>

                  <div className="grid gap-2">
                    <Label htmlFor="course-cover">封面 URL（可选）</Label>
                    <Input id="course-cover" value={courseCoverImage} onChange={(e) => setCourseCoverImage(e.target.value)} />
                  </div>

                  <div className="grid gap-2 sm:max-w-[240px]">
                    <Label>课程状态</Label>
                    <Select value={courseStatus} onValueChange={(v) => setCourseStatus(v as "0" | "1" | "2") }>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="0">草稿</SelectItem>
                        <SelectItem value="1">已发布</SelectItem>
                        <SelectItem value="2">已下架</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="grid gap-2">
                    <Label htmlFor="chapter-title">首章标题</Label>
                    <Input id="chapter-title" value={chapterTitle} onChange={(e) => setChapterTitle(e.target.value)} />
                  </div>

                  <div className="grid gap-2 sm:max-w-[180px]">
                    <Label htmlFor="chapter-sort">首章排序号</Label>
                    <Input
                      id="chapter-sort"
                      value={chapterSortOrder}
                      onChange={(e) => setChapterSortOrder(e.target.value)}
                    />
                  </div>

                  <div className="grid gap-2">
                    <Label htmlFor="chapter-content">首章 Markdown 内容</Label>
                    <textarea
                      id="chapter-content"
                      value={chapterContent}
                      onChange={(e) => setChapterContent(e.target.value)}
                      rows={8}
                      className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>

                  <Button onClick={() => void handleCreateCourse()} disabled={isCreatingCourse}>
                    {isCreatingCourse ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                    创建课程
                  </Button>
                </CardContent>
              </Card>

              <Card className="border-border">
                <CardHeader>
                  <CardTitle>已发布课程</CardTitle>
                  {isCoursesLoading && <p className="text-sm text-muted-foreground">正在同步课程列表...</p>}
                  {coursesError && <p className="text-sm text-destructive">{coursesError}</p>}
                </CardHeader>
                <CardContent>
                  {courses.length === 0 ? (
                    <p className="text-sm text-muted-foreground">暂无已发布课程。</p>
                  ) : (
                    <div className="space-y-3">
                      {courses.map((course) => (
                        <div key={course.id} className="rounded-md border border-border p-3">
                          <div className="flex items-center justify-between gap-2">
                            <p className="font-medium text-foreground">{course.title}</p>
                            <Badge variant="secondary">{course.chapterCount} 章节</Badge>
                          </div>
                          <p className="mt-1 text-sm text-muted-foreground">{course.description}</p>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </>
          )}

          {activeNav === "cdk" && (
            <>
              <div className="mb-2 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                {isCdkSyncing && <span>正在同步 CDK 数据...</span>}
                {!isCdkSyncing && !cdkSyncError && <span>已接入后端 CDK 接口</span>}
                {!isCdkSyncing && cdkSyncError && <span className="text-destructive">同步失败：{cdkSyncError}</span>}
              </div>

              <Card className="border-border">
                <CardHeader>
                  <CardTitle>CDK 管理</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex flex-col gap-3 sm:flex-row">
                    <div className="relative flex-1">
                      <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                        placeholder="按 key、用户、邮箱检索"
                        className="pl-9"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                      />
                    </div>
                    <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as "all" | "used" | "unused" | "expired") }>
                      <SelectTrigger className="sm:w-40">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">全部</SelectItem>
                        <SelectItem value="used">已使用</SelectItem>
                        <SelectItem value="unused">未使用</SelectItem>
                        <SelectItem value="expired">已过期</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Key</TableHead>
                        <TableHead>状态</TableHead>
                        <TableHead>使用人</TableHead>
                        <TableHead>创建时间</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {cdkRows.map((cdk) => (
                        <TableRow key={cdk.id}>
                          <TableCell>
                            <div className="flex items-center gap-2">
                              <code className="rounded bg-secondary px-2 py-1 text-xs">{cdk.key}</code>
                              <Button variant="ghost" size="icon" className="h-6 w-6" onClick={() => copyToClipboard(cdk.key)}>
                                {copiedKey === cdk.key ? <Check className="h-3.5 w-3.5" /> : <Copy className="h-3.5 w-3.5" />}
                              </Button>
                            </div>
                          </TableCell>
                          <TableCell>{cdk.status}</TableCell>
                          <TableCell>
                            {cdk.usedBy ? (
                              <div className="flex items-center gap-2">
                                <Avatar className="h-6 w-6">
                                  <AvatarFallback className="text-[10px]">
                                    {cdk.usedBy
                                      .split(" ")
                                      .map((part) => part[0])
                                      .join("")}
                                  </AvatarFallback>
                                </Avatar>
                                <span className="text-sm">{cdk.usedBy}</span>
                              </div>
                            ) : (
                              "-"
                            )}
                          </TableCell>
                          <TableCell>{cdk.createdAt}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            </>
          )}

          {activeNav !== "courses" && activeNav !== "cdk" && (
            <Card className="border-border">
              <CardContent className="p-6 text-sm text-muted-foreground">
                当前切片仅同步了课程与 CDK 页面，{activeNavMeta?.label ?? "该模块"} 将在后续迭代接入。
              </CardContent>
            </Card>
          )}
        </main>
      </div>
    </div>
  )
}
