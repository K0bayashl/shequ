"use client"

import { FormEvent, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { changePassword } from "@/lib/backend-api"

export function PasswordView() {
  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [form, setForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmNewPassword: "",
  })

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setErrorMessage(null)
    setSuccessMessage(null)

    if (form.newPassword !== form.confirmNewPassword) {
      setErrorMessage("两次输入的新密码不一致")
      return
    }

    setSubmitting(true)

    try {
      await changePassword(form)
      setSuccessMessage("密码修改成功，请使用新密码重新登录。")
      setForm({
        oldPassword: "",
        newPassword: "",
        confirmNewPassword: "",
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : "修改密码失败"
      setErrorMessage(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="mx-auto w-full max-w-2xl p-4 md:p-6">
      <Card>
        <CardHeader>
          <CardTitle>修改密码</CardTitle>
          <CardDescription>请输入旧密码并设置新密码。</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {errorMessage && (
            <p className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {errorMessage}
            </p>
          )}

          {successMessage && (
            <p className="rounded-md border border-emerald-300/40 bg-emerald-500/10 px-3 py-2 text-sm text-emerald-600">
              {successMessage}
            </p>
          )}

          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <label className="text-sm font-medium">旧密码</label>
              <Input
                type="password"
                value={form.oldPassword}
                onChange={(event) =>
                  setForm((prev) => ({
                    ...prev,
                    oldPassword: event.target.value,
                  }))
                }
                required
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">新密码</label>
              <Input
                type="password"
                value={form.newPassword}
                onChange={(event) =>
                  setForm((prev) => ({
                    ...prev,
                    newPassword: event.target.value,
                  }))
                }
                required
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">确认新密码</label>
              <Input
                type="password"
                value={form.confirmNewPassword}
                onChange={(event) =>
                  setForm((prev) => ({
                    ...prev,
                    confirmNewPassword: event.target.value,
                  }))
                }
                required
              />
            </div>

            <Button type="submit" disabled={submitting}>
              {submitting ? "提交中..." : "确认修改"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </main>
  )
}
