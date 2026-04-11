"use client"

import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { getCurrentUser, type UserResponse } from "@/lib/backend-api"

interface ProfileViewProps {
  initialUser?: UserResponse | null
  onProfileUpdated?: (user: UserResponse) => void
}

export function ProfileView({ initialUser = null, onProfileUpdated }: ProfileViewProps) {
  const [profile, setProfile] = useState<UserResponse | null>(initialUser)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const loadProfile = async () => {
    setLoading(true)
    setErrorMessage(null)

    try {
      const user = await getCurrentUser()
      setProfile(user)
      onProfileUpdated?.(user)
    } catch (error) {
      const message = error instanceof Error ? error.message : "获取个人信息失败"
      setErrorMessage(message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (initialUser) {
      setProfile(initialUser)
    }

    loadProfile()
  }, [])

  return (
    <main className="mx-auto w-full max-w-4xl p-4 md:p-6">
      <Card>
        <CardHeader>
          <CardTitle>个人详情</CardTitle>
          <CardDescription>查看当前登录账号的基础资料。</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {errorMessage && (
            <p className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {errorMessage}
            </p>
          )}

          <div className="grid gap-3 rounded-lg border p-4 text-sm md:grid-cols-2">
            <div>
              <p className="text-muted-foreground">用户 ID</p>
              <p className="font-medium">{profile?.id ?? "-"}</p>
            </div>
            <div>
              <p className="text-muted-foreground">用户名</p>
              <p className="font-medium">{profile?.username ?? "-"}</p>
            </div>
            <div>
              <p className="text-muted-foreground">邮箱</p>
              <p className="font-medium">{profile?.email ?? "-"}</p>
            </div>
            <div>
              <p className="text-muted-foreground">角色</p>
              <p className="font-medium">{String(profile?.role ?? "-")}</p>
            </div>
            <div>
              <p className="text-muted-foreground">状态</p>
              <p className="font-medium">{String(profile?.status ?? "-")}</p>
            </div>
            <div>
              <p className="text-muted-foreground">注册时间</p>
              <p className="font-medium">{profile?.createdAt ?? "-"}</p>
            </div>
          </div>

          <Button onClick={loadProfile} disabled={loading}>
            {loading ? "刷新中..." : "刷新资料"}
          </Button>
        </CardContent>
      </Card>
    </main>
  )
}
