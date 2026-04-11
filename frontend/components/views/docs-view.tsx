"use client"

import { useEffect, useMemo, useState } from "react"
import { BookOpen, FileText, Loader2, RefreshCw } from "lucide-react"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { ScrollArea } from "@/components/ui/scroll-area"
import { cn } from "@/lib/utils"
import {
  getChapterContent,
  getCourseDetail,
  getCourses,
  submitCourseReport,
  type ChapterContent,
  type CourseDetail,
  type CourseListItem,
} from "@/lib/backend-api"

export function DocsView() {
  const [courses, setCourses] = useState<CourseListItem[]>([])
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null)
  const [selectedChapterId, setSelectedChapterId] = useState<number | null>(null)

  const [courseDetail, setCourseDetail] = useState<CourseDetail | null>(null)
  const [chapterContent, setChapterContent] = useState<ChapterContent | null>(null)

  const [isCourseListLoading, setIsCourseListLoading] = useState(true)
  const [isCourseDetailLoading, setIsCourseDetailLoading] = useState(false)
  const [isChapterLoading, setIsChapterLoading] = useState(false)

  const [courseListError, setCourseListError] = useState<string | null>(null)
  const [courseDetailError, setCourseDetailError] = useState<string | null>(null)
  const [chapterError, setChapterError] = useState<string | null>(null)

  const [reportReasonCode, setReportReasonCode] = useState("other")
  const [reportReasonDetail, setReportReasonDetail] = useState("")
  const [isSubmittingReport, setIsSubmittingReport] = useState(false)
  const [reportError, setReportError] = useState<string | null>(null)
  const [reportSuccess, setReportSuccess] = useState<string | null>(null)

  const selectedCourse = useMemo(
    () => courses.find((course) => course.id === selectedCourseId) ?? null,
    [courses, selectedCourseId],
  )

  const reloadCourseList = async () => {
    setIsCourseListLoading(true)
    setCourseListError(null)

    try {
      const response = await getCourses()
      setCourses(response)

      if (response.length === 0) {
        setSelectedCourseId(null)
        setSelectedChapterId(null)
        setCourseDetail(null)
        setChapterContent(null)
        return
      }

      setSelectedCourseId((prev) => {
        if (prev && response.some((course) => course.id === prev)) {
          return prev
        }
        return response[0].id
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : "课程列表加载失败"
      setCourseListError(message)
    } finally {
      setIsCourseListLoading(false)
    }
  }

  useEffect(() => {
    void reloadCourseList()
  }, [])

  useEffect(() => {
    if (!selectedCourseId) {
      setCourseDetail(null)
      setSelectedChapterId(null)
      return
    }

    let cancelled = false

    const loadCourseDetail = async () => {
      setIsCourseDetailLoading(true)
      setCourseDetailError(null)

      try {
        const response = await getCourseDetail(selectedCourseId)
        if (cancelled) {
          return
        }

        setCourseDetail(response)
        setSelectedChapterId((prev) => {
          if (prev && response.chapters.some((chapter) => chapter.id === prev)) {
            return prev
          }
          return response.chapters[0]?.id ?? null
        })
      } catch (error) {
        if (cancelled) {
          return
        }
        const message = error instanceof Error ? error.message : "课程详情加载失败"
        setCourseDetailError(message)
      } finally {
        if (!cancelled) {
          setIsCourseDetailLoading(false)
        }
      }
    }

    void loadCourseDetail()

    return () => {
      cancelled = true
    }
  }, [selectedCourseId])

  useEffect(() => {
    if (!selectedCourseId || !selectedChapterId) {
      setChapterContent(null)
      return
    }

    let cancelled = false

    const loadChapterContent = async () => {
      setIsChapterLoading(true)
      setChapterError(null)

      try {
        const response = await getChapterContent(selectedCourseId, selectedChapterId)
        if (cancelled) {
          return
        }
        setChapterContent(response)
      } catch (error) {
        if (cancelled) {
          return
        }
        const message = error instanceof Error ? error.message : "章节内容加载失败"
        setChapterError(message)
      } finally {
        if (!cancelled) {
          setIsChapterLoading(false)
        }
      }
    }

    void loadChapterContent()

    return () => {
      cancelled = true
    }
  }, [selectedCourseId, selectedChapterId])

  const handleSubmitReport = async () => {
    if (!selectedCourseId) {
      setReportError("请先选择课程")
      return
    }

    setIsSubmittingReport(true)
    setReportError(null)
    setReportSuccess(null)

    try {
      const response = await submitCourseReport({
        courseId: selectedCourseId,
        reasonCode: reportReasonCode,
        reasonDetail: reportReasonDetail.trim() || undefined,
      })
      setReportSuccess(`举报已提交，编号 #${response.reportId}`)
      setReportReasonDetail("")
    } catch (error) {
      const message = error instanceof Error ? error.message : "提交举报失败"
      setReportError(message)
    } finally {
      setIsSubmittingReport(false)
    }
  }

  return (
    <div className="min-h-[calc(100vh-3.5rem)] bg-background">
      <div className="mx-auto grid max-w-7xl grid-cols-1 gap-4 p-4 lg:grid-cols-[320px_1fr] lg:p-6">
        <Card className="border-border">
          <CardHeader className="border-b border-border pb-3">
            <div className="flex items-center justify-between gap-2">
              <CardTitle className="flex items-center gap-2 text-base">
                <BookOpen className="h-4 w-4" />
                课程目录
              </CardTitle>
              <Button variant="outline" size="sm" onClick={() => void reloadCourseList()} disabled={isCourseListLoading}>
                <RefreshCw className={cn("h-4 w-4", isCourseListLoading && "animate-spin")} />
              </Button>
            </div>
            {courseListError && <p className="text-xs text-destructive">{courseListError}</p>}
          </CardHeader>

          <CardContent className="p-0">
            <ScrollArea className="h-[70vh]">
              {isCourseListLoading ? (
                <div className="flex items-center gap-2 p-4 text-sm text-muted-foreground">
                  <Loader2 className="h-4 w-4 animate-spin" />
                  正在加载课程...
                </div>
              ) : courses.length === 0 ? (
                <div className="p-4 text-sm text-muted-foreground">暂无已发布课程。</div>
              ) : (
                <div className="divide-y divide-border">
                  {courses.map((course) => {
                    const active = course.id === selectedCourseId
                    return (
                      <button
                        key={course.id}
                        onClick={() => setSelectedCourseId(course.id)}
                        className={cn(
                          "w-full px-4 py-3 text-left transition-colors hover:bg-muted/40",
                          active && "bg-accent/10",
                        )}
                      >
                        <p className={cn("font-medium", active ? "text-accent" : "text-foreground")}>{course.title}</p>
                        <p className="mt-1 line-clamp-2 text-xs text-muted-foreground">{course.description}</p>
                        <p className="mt-1 text-xs text-muted-foreground">{course.chapterCount} 章节</p>
                      </button>
                    )
                  })}
                </div>
              )}
            </ScrollArea>
          </CardContent>
        </Card>

        <Card className="border-border">
          <CardHeader className="border-b border-border pb-3">
            <div className="flex items-center gap-3">
              <Avatar className="h-8 w-8">
                <AvatarFallback>CR</AvatarFallback>
              </Avatar>
              <div>
                <CardTitle className="text-base">{selectedCourse?.title ?? "课程阅读"}</CardTitle>
                <p className="text-xs text-muted-foreground">
                  {selectedCourse ? `${selectedCourse.chapterCount} 章节` : "请选择课程后阅读"}
                </p>
              </div>
            </div>
          </CardHeader>

          <CardContent className="space-y-4 p-4 lg:p-6">
            {selectedCourse && (
              <p className="text-sm text-muted-foreground">{selectedCourse.description}</p>
            )}

            {selectedCourse && (
              <div className="space-y-3 rounded-md border border-border bg-muted/20 p-3">
                <p className="text-sm font-medium text-foreground">举报当前课程</p>
                {reportError && <p className="text-xs text-destructive">{reportError}</p>}
                {reportSuccess && <p className="text-xs text-green-600">{reportSuccess}</p>}
                <div className="flex flex-col gap-2 sm:flex-row">
                  <select
                    className="h-9 rounded-md border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring"
                    value={reportReasonCode}
                    onChange={(event) => setReportReasonCode(event.target.value)}
                  >
                    <option value="spam">垃圾信息</option>
                    <option value="abuse">辱骂攻击</option>
                    <option value="infringement">侵权内容</option>
                    <option value="other">其他</option>
                  </select>
                  <Button size="sm" onClick={() => void handleSubmitReport()} disabled={isSubmittingReport}>
                    {isSubmittingReport ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                    提交举报
                  </Button>
                </div>
                <textarea
                  value={reportReasonDetail}
                  onChange={(event) => setReportReasonDetail(event.target.value)}
                  rows={3}
                  placeholder="可选：补充举报说明"
                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
                />
              </div>
            )}

            {isCourseDetailLoading ? (
              <div className="flex items-center gap-2 rounded-md border border-border p-3 text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 animate-spin" />
                正在加载章节目录...
              </div>
            ) : courseDetailError ? (
              <div className="rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
                {courseDetailError}
              </div>
            ) : courseDetail && courseDetail.chapters.length > 0 ? (
              <div className="flex flex-wrap gap-2">
                {courseDetail.chapters.map((chapter) => (
                  <Button
                    key={chapter.id}
                    variant={chapter.id === selectedChapterId ? "default" : "outline"}
                    size="sm"
                    onClick={() => setSelectedChapterId(chapter.id)}
                  >
                    {chapter.sortOrder}. {chapter.title}
                  </Button>
                ))}
              </div>
            ) : selectedCourse ? (
              <div className="rounded-md border border-border p-3 text-sm text-muted-foreground">该课程暂无章节。</div>
            ) : null}

            {isChapterLoading ? (
              <div className="flex items-center gap-2 rounded-md border border-border p-3 text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 animate-spin" />
                正在加载章节正文...
              </div>
            ) : chapterError ? (
              <div className="rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
                {chapterError}
              </div>
            ) : chapterContent ? (
              <div className="space-y-3">
                <h3 className="flex items-center gap-2 text-lg font-semibold text-foreground">
                  <FileText className="h-4 w-4" />
                  {chapterContent.sortOrder}. {chapterContent.title}
                </h3>
                <pre className="overflow-x-auto whitespace-pre-wrap rounded-md border border-border bg-muted/20 p-4 text-sm leading-relaxed text-foreground">
                  {chapterContent.content}
                </pre>
              </div>
            ) : (
              <div className="rounded-md border border-border p-3 text-sm text-muted-foreground">请选择章节开始阅读。</div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
