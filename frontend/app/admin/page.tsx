"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { AdminView } from "@/components/views/admin-view"
import { clearAuthToken, getAuthToken, getCurrentUser } from "@/lib/backend-api"

const ADMIN_ROLE_CODE = 1

export default function AdminPage() {
  const router = useRouter()
  const [accessState, setAccessState] = useState<"checking" | "allowed" | "denied">("checking")

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      const token = getAuthToken()
      if (!token) {
        if (isMounted) {
          setAccessState("denied")
          router.replace("/")
        }
        return
      }

      try {
        const user = await getCurrentUser()
        if (!isMounted) {
          return
        }

        if (user.role === ADMIN_ROLE_CODE) {
          setAccessState("allowed")
          return
        }

        setAccessState("denied")
        router.replace("/")
      } catch {
        clearAuthToken()
        if (isMounted) {
          setAccessState("denied")
          router.replace("/")
        }
      }
    }

    void bootstrap()

    return () => {
      isMounted = false
    }
  }, [router])

  if (accessState === "checking") {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background text-sm text-muted-foreground">
        正在校验管理员权限...
      </div>
    )
  }

  if (accessState !== "allowed") {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background text-sm text-muted-foreground">
        无管理员权限，正在返回首页...
      </div>
    )
  }

  return <AdminView />
}
