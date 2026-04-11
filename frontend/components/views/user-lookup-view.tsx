"use client"

import { FormEvent, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { getUserById, type UserResponse } from "@/lib/backend-api"

export function UserLookupView() {
  const [userId, setUserId] = useState("")
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [targetUser, setTargetUser] = useState<UserResponse | null>(null)

  const handleLookup = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setErrorMessage(null)
    setTargetUser(null)

    const parsedId = Number(userId)
    if (!Number.isInteger(parsedId) || parsedId <= 0) {
      setErrorMessage("请输入合法的用户 ID（正整数）")
      return
    }

    setLoading(true)

    try {
      const user = await getUserById(parsedId)
      setTargetUser(user)
    } catch (error) {
      const message = error instanceof Error ? error.message : "查询用户失败"
      setErrorMessage(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="mx-auto w-full max-w-3xl p-4 md:p-6">
      <Card>
        <CardHeader>
          <CardTitle>按 ID 查询用户</CardTitle>
          <CardDescription>输入用户 ID，调用 /api/users/{"{id}"} 获取用户信息。</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {errorMessage && (
            <p className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {errorMessage}
            </p>
          )}

          <form className="flex flex-col gap-3 md:flex-row" onSubmit={handleLookup}>
            <Input
              value={userId}
              onChange={(event) => setUserId(event.target.value)}
              placeholder="请输入用户 ID，例如 1"
              required
            />
            <Button type="submit" disabled={loading}>
              {loading ? "查询中..." : "查询"}
            </Button>
          </form>

          {targetUser && (
            <div className="grid gap-3 rounded-lg border p-4 text-sm md:grid-cols-2">
              <div>
                <p className="text-muted-foreground">用户 ID</p>
                <p className="font-medium">{targetUser.id}</p>
              </div>
              <div>
                <p className="text-muted-foreground">用户名</p>
                <p className="font-medium">{targetUser.username}</p>
              </div>
              <div>
                <p className="text-muted-foreground">邮箱</p>
                <p className="font-medium">{targetUser.email}</p>
              </div>
              <div>
                <p className="text-muted-foreground">角色</p>
                <p className="font-medium">{String(targetUser.role)}</p>
              </div>
              <div>
                <p className="text-muted-foreground">状态</p>
                <p className="font-medium">{String(targetUser.status)}</p>
              </div>
              <div>
                <p className="text-muted-foreground">注册时间</p>
                <p className="font-medium">{targetUser.createdAt}</p>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </main>
  )
}
