export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  timestamp: string
}

export interface UserResponse {
  id: number
  username: string
  email: string
  avatar: string | null
  role: number
  status: number
  createdAt: string
}

export interface LoginResponse {
  token: string
  user: UserResponse
}

export interface LogoutResponse {
  loggedOut: boolean
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  confirmPassword: string
  cdkCode: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmNewPassword: string
}

export interface ScaffoldStatusResponse {
  application: string
  activeProfiles: string[]
  databaseEnabled: boolean
  jwtEnabled: boolean
  redisEnabled: boolean
  openapiEnabled: boolean
  generatedAt: string
}

export interface ScaffoldEchoResponse {
  message: string
  echoedAt: string
}

export interface ScaffoldSecurePingResponse {
  authenticated: boolean
  principal: string
}

export interface CommunityAuthor {
  name: string
  avatar: string
  initials: string
}

export interface CommunityThread {
  id: string
  title: string
  author: CommunityAuthor
  type: 'official' | 'user'
  timeAgo: string
  commentCount: number
  tags: string[]
  isPinned: boolean
}

export interface TopicStat {
  name: string
  count: number
}

export interface CommunityFeedResponse {
  threads: CommunityThread[]
  topics: TopicStat[]
}

export interface AdminCdkItem {
  id: number
  key: string
  status: 'used' | 'unused' | 'expired'
  usedBy: string | null
  usedByEmail: string | null
  usedAt: string | null
  createdAt: string
}

export interface AdminCdkOverviewResponse {
  totalUsers: number
  activeMembers: number
  remainingCdks: number
  cdks: AdminCdkItem[]
}

export interface CreateAdminCdkRequest {
  code: string
}

export interface CreateAdminCdkResponse {
  id: number
  key: string
  status: 'unused'
  createdAt: string | null
}

export interface DocsChapterItem {
  title: string
  href: string
  active: boolean
}

export interface DocsChapter {
  title: string
  iconKey: string
  items: DocsChapterItem[]
}

export interface DocsChapterListResponse {
  chapters: DocsChapter[]
}

export interface CreateCourseChapterRequest {
  title: string
  content: string
  sortOrder: number
}

export interface CreateCourseRequest {
  title: string
  description: string
  coverImage?: string
  status: 0 | 1 | 2
  chapters: CreateCourseChapterRequest[]
}

export interface CreateCourseResponse {
  courseId: number
  status: number
  chapterCount: number
}

export interface UpdateCourseRequest {
  title: string
  description: string
  coverImage?: string
  status: 0 | 1 | 2
}

export interface UpdateCourseResponse {
  courseId: number
  status: number
}

export interface DeleteCourseResponse {
  courseId: number
  status: number
}

export interface UpdateCourseChapterRequest {
  title: string
  content: string
  sortOrder: number
}

export interface UpdateCourseChapterResponse {
  courseId: number
  chapterId: number
  sortOrder: number
}

export interface CourseListItem {
  id: number
  title: string
  description: string
  coverImage: string | null
  chapterCount: number
  publishedAt: string
}

export interface CourseChapterItem {
  id: number
  title: string
  sortOrder: number
}

export interface CourseDetail {
  id: number
  title: string
  description: string
  coverImage: string | null
  chapters: CourseChapterItem[]
}

export interface ChapterContent {
  courseId: number
  chapterId: number
  title: string
  sortOrder: number
  content: string
}

export type ModerationReportStatusFilter = 'all' | 'pending' | 'resolved' | 'rejected'

export interface SubmitCourseReportRequest {
  courseId: number
  reasonCode: string
  reasonDetail?: string
}

export interface SubmitCourseReportResponse {
  reportId: number
  status: number
}

export interface ModerationReportItem {
  reportId: number
  contentType: string
  contentId: number
  reporterUserId: number
  reasonCode: string
  reasonDetail: string | null
  status: number
  handledBy: number | null
  handledAt: string | null
  handleNote: string | null
  createdAt: string
}

export interface HandleCourseReportRequest {
  decision: 'approve' | 'reject'
  handleNote?: string
  takedownCourse: boolean
  banAuthor: boolean
}

export interface HandleCourseReportResponse {
  reportId: number
  status: number
  courseTakenDown: boolean
  authorBanned: boolean
}

export interface CourseModerationRequest {
  reason?: string
}

export interface CourseModerationResponse {
  courseId: number
  status: number
}

export interface UserModerationRequest {
  reason?: string
}

export interface UserModerationResponse {
  userId: number
  status: number
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080'
export const AUTH_TOKEN_KEY = 'community_mvp_auth_token'

function toAbsoluteUrl(path: string): string {
  return `${API_BASE_URL}${path}`
}

export function getAuthToken(): string | null {
  if (typeof window === 'undefined') {
    return null
  }
  return window.localStorage.getItem(AUTH_TOKEN_KEY)
}

export function setAuthToken(token: string): void {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.setItem(AUTH_TOKEN_KEY, token)
}

export function clearAuthToken(): void {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.removeItem(AUTH_TOKEN_KEY)
}

async function request<T>(
  path: string,
  init: RequestInit = {},
  authRequired = false,
): Promise<T> {
  const headers = new Headers(init.headers)

  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (authRequired) {
    const token = getAuthToken()
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }
  }

  const response = await fetch(toAbsoluteUrl(path), {
    ...init,
    headers,
    cache: 'no-store',
  })

  let payload: ApiResponse<T> | null = null
  try {
    payload = (await response.json()) as ApiResponse<T>
  } catch {
    payload = null
  }

  if (!response.ok) {
    throw new Error(payload?.message || `请求失败（${response.status}）`)
  }

  if (!payload) {
    throw new Error('服务端响应格式异常')
  }

  if (payload.code !== 'SUCCESS') {
    throw new Error(payload.message || '业务处理失败')
  }

  return payload.data
}

export function registerUser(body: RegisterRequest): Promise<UserResponse> {
  return request<UserResponse>(
    '/api/users/register',
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    false,
  )
}

export function loginUser(body: LoginRequest): Promise<LoginResponse> {
  return request<LoginResponse>(
    '/api/users/login',
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    false,
  )
}

export function logoutUser(): Promise<LogoutResponse> {
  return request<LogoutResponse>(
    '/api/users/logout',
    {
      method: 'POST',
    },
    true,
  )
}

export function getCurrentUser(): Promise<UserResponse> {
  return request<UserResponse>('/api/users/me', { method: 'GET' }, true)
}

export function getUserById(id: number): Promise<UserResponse> {
  return request<UserResponse>(`/api/users/${id}`, { method: 'GET' }, true)
}

export function changePassword(body: ChangePasswordRequest): Promise<UserResponse> {
  return request<UserResponse>(
    '/api/users/password/change',
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function scaffoldPing(): Promise<ScaffoldStatusResponse> {
  return request<ScaffoldStatusResponse>('/api/v1/scaffold/ping', { method: 'GET' }, false)
}

export function scaffoldEcho(message: string): Promise<ScaffoldEchoResponse> {
  return request<ScaffoldEchoResponse>(
    '/api/v1/scaffold/echo',
    {
      method: 'POST',
      body: JSON.stringify({ message }),
    },
    false,
  )
}

export function scaffoldSecurePing(): Promise<ScaffoldSecurePingResponse> {
  return request<ScaffoldSecurePingResponse>('/api/v1/scaffold/secure-ping', { method: 'GET' }, true)
}

export function getCommunityFeed(
  filter: 'all' | 'official' | 'user' = 'all',
  sort: 'latest' | 'hot' | 'top' = 'latest',
): Promise<CommunityFeedResponse> {
  const params = new URLSearchParams({ filter, sort })
  return request<CommunityFeedResponse>(`/api/v1/content/community/feed?${params.toString()}`, { method: 'GET' }, true)
}

export function getAdminCdks(
  search = '',
  status: 'all' | 'used' | 'unused' | 'expired' = 'all',
): Promise<AdminCdkOverviewResponse> {
  const params = new URLSearchParams()
  params.set('status', status)
  if (search.trim()) {
    params.set('search', search.trim())
  }
  return request<AdminCdkOverviewResponse>(`/api/v1/content/admin/cdks?${params.toString()}`, { method: 'GET' }, true)
}

export function createAdminCdk(body: CreateAdminCdkRequest): Promise<CreateAdminCdkResponse> {
  return request<CreateAdminCdkResponse>(
    '/api/v1/content/admin/cdks',
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function getDocsChapters(): Promise<DocsChapterListResponse> {
  return request<DocsChapterListResponse>('/api/v1/content/docs/chapters', { method: 'GET' }, true)
}

export function createCourse(body: CreateCourseRequest): Promise<CreateCourseResponse> {
  return request<CreateCourseResponse>(
    '/api/admin/courses',
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function updateCourseByAdmin(
  courseId: number,
  body: UpdateCourseRequest,
): Promise<UpdateCourseResponse> {
  return request<UpdateCourseResponse>(
    `/api/admin/courses/${courseId}`,
    {
      method: 'PUT',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function deleteCourseByAdmin(courseId: number): Promise<DeleteCourseResponse> {
  return request<DeleteCourseResponse>(
    `/api/admin/courses/${courseId}`,
    {
      method: 'DELETE',
    },
    true,
  )
}

export function updateCourseChapterByAdmin(
  courseId: number,
  chapterId: number,
  body: UpdateCourseChapterRequest,
): Promise<UpdateCourseChapterResponse> {
  return request<UpdateCourseChapterResponse>(
    `/api/admin/courses/${courseId}/chapters/${chapterId}`,
    {
      method: 'PUT',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function getCourses(): Promise<CourseListItem[]> {
  return request<CourseListItem[]>('/api/courses', { method: 'GET' }, true)
}

export function getCourseDetail(courseId: number): Promise<CourseDetail> {
  return request<CourseDetail>(`/api/courses/${courseId}`, { method: 'GET' }, true)
}

export function getChapterContent(courseId: number, chapterId: number): Promise<ChapterContent> {
  return request<ChapterContent>(`/api/courses/${courseId}/chapters/${chapterId}`, { method: 'GET' }, true)
}

export function submitCourseReport(body: SubmitCourseReportRequest): Promise<SubmitCourseReportResponse> {
  return request<SubmitCourseReportResponse>(
    '/api/reports',
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function getModerationReports(
  status: ModerationReportStatusFilter = 'all',
): Promise<ModerationReportItem[]> {
  const params = new URLSearchParams({ status })
  return request<ModerationReportItem[]>(`/api/admin/moderation/reports?${params.toString()}`, { method: 'GET' }, true)
}

export function handleCourseReport(
  reportId: number,
  body: HandleCourseReportRequest,
): Promise<HandleCourseReportResponse> {
  return request<HandleCourseReportResponse>(
    `/api/admin/moderation/reports/${reportId}/handle`,
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function takedownCourse(
  courseId: number,
  body: CourseModerationRequest = {},
): Promise<CourseModerationResponse> {
  return request<CourseModerationResponse>(
    `/api/admin/moderation/courses/${courseId}/takedown`,
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function restoreCourse(
  courseId: number,
  body: CourseModerationRequest = {},
): Promise<CourseModerationResponse> {
  return request<CourseModerationResponse>(
    `/api/admin/moderation/courses/${courseId}/restore`,
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function banUser(
  userId: number,
  body: UserModerationRequest = {},
): Promise<UserModerationResponse> {
  return request<UserModerationResponse>(
    `/api/admin/moderation/users/${userId}/ban`,
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}

export function unbanUser(
  userId: number,
  body: UserModerationRequest = {},
): Promise<UserModerationResponse> {
  return request<UserModerationResponse>(
    `/api/admin/moderation/users/${userId}/unban`,
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
    true,
  )
}
