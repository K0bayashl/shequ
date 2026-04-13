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
  banUser,
  createAdminCdk,
  createCourse,
  deleteCourseByAdmin,
  getChapterContent,
  getCourseDetail,
  getAdminCdks,
  getCourses,
  getModerationReports,
  handleCourseReport,
  restoreCourse,
  takedownCourse,
  unbanUser,
  updateCourseChapterByAdmin,
  updateCourseByAdmin,
  type AdminCdkItem,
  type CourseChapterItem,
  type CourseListItem,
  type CreateCourseRequest,
  type ModerationReportItem,
  type ModerationReportStatusFilter,
  type UpdateCourseRequest,
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
  const [isCreatingCdk, setIsCreatingCdk] = useState(false)
  const [createCdkError, setCreateCdkError] = useState<string | null>(null)
  const [createCdkSuccess, setCreateCdkSuccess] = useState<string | null>(null)

  const [courses, setCourses] = useState<CourseListItem[]>([])
  const [isCoursesLoading, setIsCoursesLoading] = useState(false)
  const [coursesError, setCoursesError] = useState<string | null>(null)
  const [courseManagingId, setCourseManagingId] = useState<number | null>(null)
  const [courseManageError, setCourseManageError] = useState<string | null>(null)
  const [courseManageSuccess, setCourseManageSuccess] = useState<string | null>(null)
  const [isCreatingCourse, setIsCreatingCourse] = useState(false)
  const [createCourseError, setCreateCourseError] = useState<string | null>(null)
  const [createCourseSuccess, setCreateCourseSuccess] = useState<string | null>(null)

  const [reports, setReports] = useState<ModerationReportItem[]>([])
  const [reportStatusFilter, setReportStatusFilter] = useState<ModerationReportStatusFilter>("pending")
  const [isReportsLoading, setIsReportsLoading] = useState(false)
  const [reportsError, setReportsError] = useState<string | null>(null)
  const [handlingReportId, setHandlingReportId] = useState<number | null>(null)

  const [targetCourseId, setTargetCourseId] = useState("")
  const [courseActionReason, setCourseActionReason] = useState("")
  const [courseActionLoading, setCourseActionLoading] = useState<"takedown" | "restore" | null>(null)

  const [targetUserId, setTargetUserId] = useState("")
  const [userActionReason, setUserActionReason] = useState("")
  const [userActionLoading, setUserActionLoading] = useState<"ban" | "unban" | null>(null)

  const [moderationActionError, setModerationActionError] = useState<string | null>(null)
  const [moderationActionSuccess, setModerationActionSuccess] = useState<string | null>(null)

  const [courseTitle, setCourseTitle] = useState("")
  const [courseDescription, setCourseDescription] = useState("")
  const [courseCoverImage, setCourseCoverImage] = useState("")
  const [courseStatus, setCourseStatus] = useState<"0" | "1" | "2">("1")
  const [chapterTitle, setChapterTitle] = useState("")
  const [chapterContent, setChapterContent] = useState("")
  const [chapterSortOrder, setChapterSortOrder] = useState("1")

  const [chapterEditCourseId, setChapterEditCourseId] = useState<number | null>(null)
  const [chapterEditOptions, setChapterEditOptions] = useState<CourseChapterItem[]>([])
  const [chapterEditId, setChapterEditId] = useState("")
  const [chapterEditTitle, setChapterEditTitle] = useState("")
  const [chapterEditContent, setChapterEditContent] = useState("")
  const [chapterEditSortOrder, setChapterEditSortOrder] = useState("")
  const [isChapterEditorLoading, setIsChapterEditorLoading] = useState(false)
  const [isChapterSaving, setIsChapterSaving] = useState(false)

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

  const syncReports = useCallback(async (status: ModerationReportStatusFilter = reportStatusFilter) => {
    setIsReportsLoading(true)
    setReportsError(null)

    try {
      const response = await getModerationReports(status)
      setReports(response)
    } catch (error) {
      const message = error instanceof Error ? error.message : "举报列表同步失败"
      setReportsError(message)
    } finally {
      setIsReportsLoading(false)
    }
  }, [reportStatusFilter])

  useEffect(() => {
    if (activeNav === "cdk") {
      void syncAdminCdks()
    }
    if (activeNav === "courses") {
      void syncCourses()
    }
    if (activeNav === "moderation") {
      void syncReports()
    }
  }, [activeNav, syncAdminCdks, syncCourses, syncReports])

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

  useEffect(() => {
    if (activeNav !== "moderation") {
      return
    }
    void syncReports(reportStatusFilter)
  }, [activeNav, reportStatusFilter, syncReports])

  const copyToClipboard = (key: string) => {
    navigator.clipboard.writeText(key)
    setCopiedKey(key)
    setTimeout(() => setCopiedKey(null), 1500)
  }

  const generateRandomCdkCode = () => {
    const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    const randomPart = (length: number) => {
      const values = new Uint32Array(length)
      window.crypto.getRandomValues(values)
      return Array.from(values)
        .map((value) => chars[value % chars.length])
        .join("")
    }

    const year = new Date().getFullYear()
    return `CDK-${year}-${randomPart(4)}-${randomPart(4)}`
  }

  const handleCreateCdk = async () => {
    const normalizedCode = generateRandomCdkCode()

    setCreateCdkError(null)
    setCreateCdkSuccess(null)
    setIsCreatingCdk(true)

    try {
      const response = await createAdminCdk({ code: normalizedCode })
      setCreateCdkSuccess(`CDK 已创建：${response.key}`)
      await syncAdminCdks()
    } catch (error) {
      const message = error instanceof Error ? error.message : "创建 CDK 失败"
      setCreateCdkError(message)
    } finally {
      setIsCreatingCdk(false)
    }
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

  const handleEditCourse = async (course: CourseListItem) => {
    const nextTitle = window.prompt("请输入新的课程标题", course.title)
    if (nextTitle === null) {
      return
    }
    const nextDescription = window.prompt("请输入新的课程简介", course.description)
    if (nextDescription === null) {
      return
    }
    const nextStatusRaw = window.prompt("请输入课程状态（0=草稿，1=已发布，2=已下架）", "1")
    if (nextStatusRaw === null) {
      return
    }

    const parsedStatus = Number(nextStatusRaw)
    if (![0, 1, 2].includes(parsedStatus)) {
      setCourseManageError("课程状态必须是 0、1 或 2")
      return
    }

    const payload: UpdateCourseRequest = {
      title: nextTitle.trim(),
      description: nextDescription.trim(),
      status: parsedStatus as 0 | 1 | 2,
      coverImage: course.coverImage ?? undefined,
    }

    if (!payload.title || !payload.description) {
      setCourseManageError("课程标题和简介不能为空")
      return
    }

    setCourseManageError(null)
    setCourseManageSuccess(null)
    setCourseManagingId(course.id)
    try {
      const response = await updateCourseByAdmin(course.id, payload)
      setCourseManageSuccess(`课程 ${response.courseId} 已更新，状态=${response.status}`)
      await syncCourses()
    } catch (error) {
      const message = error instanceof Error ? error.message : "课程编辑失败"
      setCourseManageError(message)
    } finally {
      setCourseManagingId(null)
    }
  }

  const handleDeleteCourse = async (course: CourseListItem) => {
    const confirmed = window.confirm(`确认软删除课程《${course.title}》吗？`)
    if (!confirmed) {
      return
    }

    setCourseManageError(null)
    setCourseManageSuccess(null)
    setCourseManagingId(course.id)
    try {
      const response = await deleteCourseByAdmin(course.id)
      setCourseManageSuccess(`课程 ${response.courseId} 已软删除（状态=${response.status}）`)
      await syncCourses()
    } catch (error) {
      const message = error instanceof Error ? error.message : "课程删除失败"
      setCourseManageError(message)
    } finally {
      setCourseManagingId(null)
    }
  }

  const resetChapterEditor = () => {
    setChapterEditCourseId(null)
    setChapterEditOptions([])
    setChapterEditId("")
    setChapterEditTitle("")
    setChapterEditContent("")
    setChapterEditSortOrder("")
  }

  const loadChapterEditorContent = async (courseId: number, chapterId: number) => {
    const chapter = await getChapterContent(courseId, chapterId)
    setChapterEditId(String(chapter.chapterId))
    setChapterEditTitle(chapter.title)
    setChapterEditContent(chapter.content)
    setChapterEditSortOrder(String(chapter.sortOrder))
  }

  const handleEditChapter = async (course: CourseListItem) => {
    if (chapterEditCourseId === course.id) {
      resetChapterEditor()
      return
    }

    setCourseManageError(null)
    setCourseManageSuccess(null)
    setIsChapterEditorLoading(true)
    setCourseManagingId(course.id)

    try {
      const detail = await getCourseDetail(course.id)
      if (detail.chapters.length === 0) {
        setCourseManageError("该课程暂无可编辑章节")
        return
      }

      setChapterEditCourseId(course.id)
      setChapterEditOptions(detail.chapters)
      await loadChapterEditorContent(course.id, detail.chapters[0].id)
    } catch (error) {
      const message = error instanceof Error ? error.message : "章节编辑失败"
      setCourseManageError(message)
    } finally {
      setIsChapterEditorLoading(false)
      setCourseManagingId(null)
    }
  }

  const handleChangeEditChapter = async (chapterId: string) => {
    if (chapterEditCourseId === null) {
      return
    }

    const parsedChapterId = Number(chapterId)
    if (!Number.isInteger(parsedChapterId) || parsedChapterId <= 0) {
      setCourseManageError("章节 ID 必须是大于 0 的整数")
      return
    }

    setCourseManageError(null)
    setIsChapterEditorLoading(true)
    try {
      await loadChapterEditorContent(chapterEditCourseId, parsedChapterId)
    } catch (error) {
      const message = error instanceof Error ? error.message : "加载章节详情失败"
      setCourseManageError(message)
    } finally {
      setIsChapterEditorLoading(false)
    }
  }

  const handleSubmitChapterEdit = async () => {
    if (chapterEditCourseId === null) {
      setCourseManageError("请先选择要编辑的课程")
      return
    }

    const parsedChapterId = Number(chapterEditId)
    if (!Number.isInteger(parsedChapterId) || parsedChapterId <= 0) {
      setCourseManageError("章节 ID 必须是大于 0 的整数")
      return
    }

    const parsedSortOrder = Number(chapterEditSortOrder)
    if (!Number.isInteger(parsedSortOrder) || parsedSortOrder <= 0) {
      setCourseManageError("章节排序号必须是大于 0 的整数")
      return
    }

    if (!chapterEditTitle.trim() || !chapterEditContent.trim()) {
      setCourseManageError("章节标题和内容不能为空")
      return
    }

    setCourseManageError(null)
    setCourseManageSuccess(null)
    setIsChapterSaving(true)
    setCourseManagingId(chapterEditCourseId)

    try {
      const response = await updateCourseChapterByAdmin(chapterEditCourseId, parsedChapterId, {
        title: chapterEditTitle.trim(),
        content: chapterEditContent,
        sortOrder: parsedSortOrder,
      })
      setCourseManageSuccess(`课程 ${response.courseId} 的章节 ${response.chapterId} 已更新（排序=${response.sortOrder}）`)

      const detail = await getCourseDetail(chapterEditCourseId)
      setChapterEditOptions(detail.chapters)
      await loadChapterEditorContent(chapterEditCourseId, parsedChapterId)
      await syncCourses()
    } catch (error) {
      const message = error instanceof Error ? error.message : "章节编辑失败"
      setCourseManageError(message)
    } finally {
      setIsChapterSaving(false)
      setCourseManagingId(null)
    }
  }

  const handleReportDecision = async (
    reportId: number,
    decision: "approve" | "reject",
    takedown: boolean,
    banAuthorAction: boolean,
  ) => {
    setModerationActionError(null)
    setModerationActionSuccess(null)
    const handleNote = window.prompt("请输入处理备注（可留空）") ?? ""
    setHandlingReportId(reportId)

    try {
      const response = await handleCourseReport(reportId, {
        decision,
        handleNote,
        takedownCourse: takedown,
        banAuthor: banAuthorAction,
      })
      setModerationActionSuccess(
        `举报 #${response.reportId} 处理成功（状态=${response.status}，下架=${response.courseTakenDown ? "是" : "否"}，封禁=${response.authorBanned ? "是" : "否"}）`,
      )
      await syncReports(reportStatusFilter)
      await syncCourses()
    } catch (error) {
      const message = error instanceof Error ? error.message : "处理举报失败"
      setModerationActionError(message)
    } finally {
      setHandlingReportId(null)
    }
  }

  const handleCourseAction = async (action: "takedown" | "restore") => {
    setModerationActionError(null)
    setModerationActionSuccess(null)

    const parsedCourseId = Number(targetCourseId)
    if (!Number.isInteger(parsedCourseId) || parsedCourseId <= 0) {
      setModerationActionError("课程 ID 必须是大于 0 的整数")
      return
    }

    setCourseActionLoading(action)
    try {
      const response = action === "takedown"
        ? await takedownCourse(parsedCourseId, { reason: courseActionReason.trim() || undefined })
        : await restoreCourse(parsedCourseId, { reason: courseActionReason.trim() || undefined })
      setModerationActionSuccess(`课程 ${response.courseId} 操作成功，当前状态=${response.status}`)
      await syncCourses()
      await syncReports(reportStatusFilter)
    } catch (error) {
      const message = error instanceof Error ? error.message : "课程治理操作失败"
      setModerationActionError(message)
    } finally {
      setCourseActionLoading(null)
    }
  }

  const handleUserAction = async (action: "ban" | "unban") => {
    setModerationActionError(null)
    setModerationActionSuccess(null)

    const parsedUserId = Number(targetUserId)
    if (!Number.isInteger(parsedUserId) || parsedUserId <= 0) {
      setModerationActionError("用户 ID 必须是大于 0 的整数")
      return
    }

    setUserActionLoading(action)
    try {
      const response = action === "ban"
        ? await banUser(parsedUserId, { reason: userActionReason.trim() || undefined })
        : await unbanUser(parsedUserId, { reason: userActionReason.trim() || undefined })
      setModerationActionSuccess(`用户 ${response.userId} 操作成功，当前状态=${response.status}`)
      await syncReports(reportStatusFilter)
    } catch (error) {
      const message = error instanceof Error ? error.message : "用户治理操作失败"
      setModerationActionError(message)
    } finally {
      setUserActionLoading(null)
    }
  }

  const formatReportStatus = (status: number) => {
    if (status === 0) {
      return "待处理"
    }
    if (status === 1) {
      return "已通过"
    }
    if (status === 2) {
      return "已驳回"
    }
    return `未知(${status})`
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
                  : activeNav === "moderation"
                    ? "处理举报并执行课程/用户治理动作"
                  : activeNav === "cdk"
                    ? "管理 CDK 激活码"
                    : "该模块尚未在当前切片实现"}
              </p>
            </div>
          </div>

          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              if (activeNav === "courses") {
                void syncCourses()
                return
              }
              if (activeNav === "moderation") {
                void syncReports(reportStatusFilter)
                return
              }
              void syncAdminCdks()
            }}
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
                  {courseManageError && <p className="text-sm text-destructive">{courseManageError}</p>}
                  {courseManageSuccess && <p className="text-sm text-green-600">{courseManageSuccess}</p>}
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
                          <div className="mt-3 flex flex-wrap justify-end gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              disabled={courseManagingId === course.id}
                              onClick={() => void handleEditChapter(course)}
                            >
                              {courseManagingId === course.id ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                              编辑章节
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              disabled={courseManagingId === course.id}
                              onClick={() => void handleEditCourse(course)}
                            >
                              {courseManagingId === course.id ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                              编辑课程
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              disabled={courseManagingId === course.id}
                              onClick={() => void handleDeleteCourse(course)}
                            >
                              {courseManagingId === course.id ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                              软删除
                            </Button>
                          </div>

                          {chapterEditCourseId === course.id && (
                            <div className="mt-4 space-y-3 rounded-md border border-dashed border-border p-3">
                              <p className="text-sm font-medium text-foreground">章节编辑表单</p>
                              {isChapterEditorLoading && (
                                <p className="text-xs text-muted-foreground">正在加载章节详情...</p>
                              )}
                              <div className="grid gap-2 sm:max-w-[320px]">
                                <Label>选择章节</Label>
                                <Select
                                  value={chapterEditId}
                                  onValueChange={(v) => void handleChangeEditChapter(v)}
                                  disabled={isChapterEditorLoading || isChapterSaving}
                                >
                                  <SelectTrigger>
                                    <SelectValue placeholder="选择要编辑的章节" />
                                  </SelectTrigger>
                                  <SelectContent>
                                    {chapterEditOptions.map((chapter) => (
                                      <SelectItem key={chapter.id} value={String(chapter.id)}>
                                        {chapter.title}（排序 {chapter.sortOrder}）
                                      </SelectItem>
                                    ))}
                                  </SelectContent>
                                </Select>
                              </div>

                              <div className="grid gap-2">
                                <Label htmlFor={`chapter-edit-title-${course.id}`}>章节标题</Label>
                                <Input
                                  id={`chapter-edit-title-${course.id}`}
                                  value={chapterEditTitle}
                                  onChange={(e) => setChapterEditTitle(e.target.value)}
                                  disabled={isChapterEditorLoading || isChapterSaving}
                                />
                              </div>

                              <div className="grid gap-2 sm:max-w-[180px]">
                                <Label htmlFor={`chapter-edit-sort-${course.id}`}>章节排序号</Label>
                                <Input
                                  id={`chapter-edit-sort-${course.id}`}
                                  value={chapterEditSortOrder}
                                  onChange={(e) => setChapterEditSortOrder(e.target.value)}
                                  disabled={isChapterEditorLoading || isChapterSaving}
                                />
                              </div>

                              <div className="grid gap-2">
                                <Label htmlFor={`chapter-edit-content-${course.id}`}>章节 Markdown 内容</Label>
                                <textarea
                                  id={`chapter-edit-content-${course.id}`}
                                  value={chapterEditContent}
                                  onChange={(e) => setChapterEditContent(e.target.value)}
                                  rows={8}
                                  disabled={isChapterEditorLoading || isChapterSaving}
                                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-60"
                                />
                              </div>

                              <div className="flex flex-wrap justify-end gap-2">
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={resetChapterEditor}
                                  disabled={isChapterSaving}
                                >
                                  取消
                                </Button>
                                <Button
                                  size="sm"
                                  onClick={() => void handleSubmitChapterEdit()}
                                  disabled={isChapterEditorLoading || isChapterSaving}
                                >
                                  {isChapterSaving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                                  保存章节
                                </Button>
                              </div>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </>
          )}

          {activeNav === "moderation" && (
            <>
              {moderationActionError && <p className="text-sm text-destructive">{moderationActionError}</p>}
              {moderationActionSuccess && <p className="text-sm text-green-600">{moderationActionSuccess}</p>}

              <Card className="border-border">
                <CardHeader>
                  <div className="flex items-center justify-between gap-3">
                    <CardTitle>举报列表</CardTitle>
                    <div className="flex items-center gap-2">
                      <Select
                        value={reportStatusFilter}
                        onValueChange={(v) => setReportStatusFilter(v as ModerationReportStatusFilter)}
                      >
                        <SelectTrigger className="w-[180px]">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="all">全部</SelectItem>
                          <SelectItem value="pending">待处理</SelectItem>
                          <SelectItem value="resolved">已通过</SelectItem>
                          <SelectItem value="rejected">已驳回</SelectItem>
                        </SelectContent>
                      </Select>
                      <Button variant="outline" size="sm" onClick={() => void syncReports(reportStatusFilter)}>
                        <RefreshCw className={"mr-2 h-4 w-4"} />
                        刷新列表
                      </Button>
                    </div>
                  </div>
                  {isReportsLoading && <p className="text-sm text-muted-foreground">正在同步举报列表...</p>}
                  {reportsError && <p className="text-sm text-destructive">{reportsError}</p>}
                </CardHeader>
                <CardContent>
                  {reports.length === 0 ? (
                    <p className="text-sm text-muted-foreground">当前筛选下暂无举报。</p>
                  ) : (
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>ID</TableHead>
                          <TableHead>内容</TableHead>
                          <TableHead>举报原因</TableHead>
                          <TableHead>状态</TableHead>
                          <TableHead>时间</TableHead>
                          <TableHead>操作</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {reports.map((report) => (
                          <TableRow key={report.reportId}>
                            <TableCell>#{report.reportId}</TableCell>
                            <TableCell>{report.contentType}#{report.contentId}</TableCell>
                            <TableCell>
                              <p className="text-sm font-medium">{report.reasonCode}</p>
                              {report.reasonDetail && (
                                <p className="text-xs text-muted-foreground">{report.reasonDetail}</p>
                              )}
                            </TableCell>
                            <TableCell>
                              <Badge variant={report.status === 0 ? "secondary" : "outline"}>
                                {formatReportStatus(report.status)}
                              </Badge>
                            </TableCell>
                            <TableCell>{new Date(report.createdAt).toLocaleString()}</TableCell>
                            <TableCell>
                              {report.status === 0 ? (
                                <div className="flex flex-wrap gap-2">
                                  <Button
                                    size="sm"
                                    onClick={() => void handleReportDecision(report.reportId, "approve", true, true)}
                                    disabled={handlingReportId === report.reportId}
                                  >
                                    {handlingReportId === report.reportId ? (
                                      <Loader2 className="mr-2 h-3.5 w-3.5 animate-spin" />
                                    ) : null}
                                    通过并联动处置
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => void handleReportDecision(report.reportId, "reject", false, false)}
                                    disabled={handlingReportId === report.reportId}
                                  >
                                    驳回
                                  </Button>
                                </div>
                              ) : (
                                <span className="text-xs text-muted-foreground">已处理</span>
                              )}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  )}
                </CardContent>
              </Card>

              <Card className="border-border">
                <CardHeader>
                  <CardTitle>课程治理动作</CardTitle>
                </CardHeader>
                <CardContent className="grid gap-3 sm:grid-cols-2">
                  <div className="grid gap-2">
                    <Label htmlFor="moderation-course-id">课程 ID</Label>
                    <Input
                      id="moderation-course-id"
                      value={targetCourseId}
                      onChange={(e) => setTargetCourseId(e.target.value)}
                      placeholder="例如 1001"
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="moderation-course-reason">操作原因（可选）</Label>
                    <Input
                      id="moderation-course-reason"
                      value={courseActionReason}
                      onChange={(e) => setCourseActionReason(e.target.value)}
                      placeholder="例如：人工复核结论"
                    />
                  </div>
                  <div className="sm:col-span-2 flex flex-wrap gap-2">
                    <Button
                      variant="outline"
                      onClick={() => void handleCourseAction("takedown")}
                      disabled={courseActionLoading !== null}
                    >
                      {courseActionLoading === "takedown" ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                      下架课程
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => void handleCourseAction("restore")}
                      disabled={courseActionLoading !== null}
                    >
                      {courseActionLoading === "restore" ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                      恢复课程
                    </Button>
                  </div>
                </CardContent>
              </Card>

              <Card className="border-border">
                <CardHeader>
                  <CardTitle>用户治理动作</CardTitle>
                </CardHeader>
                <CardContent className="grid gap-3 sm:grid-cols-2">
                  <div className="grid gap-2">
                    <Label htmlFor="moderation-user-id">用户 ID</Label>
                    <Input
                      id="moderation-user-id"
                      value={targetUserId}
                      onChange={(e) => setTargetUserId(e.target.value)}
                      placeholder="例如 2001"
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="moderation-user-reason">操作原因（可选）</Label>
                    <Input
                      id="moderation-user-reason"
                      value={userActionReason}
                      onChange={(e) => setUserActionReason(e.target.value)}
                      placeholder="例如：重复违规"
                    />
                  </div>
                  <div className="sm:col-span-2 flex flex-wrap gap-2">
                    <Button
                      variant="outline"
                      onClick={() => void handleUserAction("ban")}
                      disabled={userActionLoading !== null}
                    >
                      {userActionLoading === "ban" ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                      封禁用户
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => void handleUserAction("unban")}
                      disabled={userActionLoading !== null}
                    >
                      {userActionLoading === "unban" ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                      解封用户
                    </Button>
                  </div>
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
                  {createCdkError && <p className="text-sm text-destructive">{createCdkError}</p>}
                  {createCdkSuccess && <p className="text-sm text-green-600">{createCdkSuccess}</p>}

                  <div className="flex items-center justify-between rounded-md border border-dashed border-border px-3 py-2">
                    <p className="text-sm text-muted-foreground">点击按钮后将随机生成一个未使用 CDK</p>
                    <Button onClick={() => void handleCreateCdk()} disabled={isCreatingCdk}>
                      {isCreatingCdk ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                      随机创建 CDK
                    </Button>
                  </div>

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

          {activeNav !== "courses" && activeNav !== "cdk" && activeNav !== "moderation" && (
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
