"use client"

import { useEffect, useState } from "react"
import { Key, BookOpen, Users, Shield, LogOut, Settings, Search, User, ChevronDown } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { AuthView } from "@/components/views/auth-view"
import { DocsView } from "@/components/views/docs-view"
import { CommunityView } from "@/components/views/community-view"
import { AdminView } from "@/components/views/admin-view"
import { ProfileView } from "@/components/views/profile-view"
import { PasswordView } from "@/components/views/password-view"
import { UserLookupView } from "@/components/views/user-lookup-view"
import {
  clearAuthToken,
  getAuthToken,
  getCurrentUser,
  logoutUser,
  type UserResponse,
} from "@/lib/backend-api"

type ViewType =
  | "auth"
  | "courses"
  | "community"
  | "admin"
  | "profile"
  | "password"
  | "userLookup"

const navItems = [
  { id: "courses" as ViewType, label: "Courses", icon: BookOpen },
  { id: "community" as ViewType, label: "Community", icon: Users },
  { id: "admin" as ViewType, label: "Admin", icon: Shield },
]

function GlobalNavbar({
  currentView,
  onNavigate,
  onLogout,
  currentUser,
  isLoggingOut,
}: {
  currentView: ViewType
  onNavigate: (view: ViewType) => void
  onLogout: () => void
  currentUser: UserResponse | null
  isLoggingOut: boolean
}) {
  const displayName = currentUser?.username || "yali_user"
  const displayEmail = currentUser?.email || "user@yali.dev"
  const displayInitials = (displayName[0] || "Y").toUpperCase()

  return (
    <header className="sticky top-0 z-50 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="mx-auto flex h-14 items-center justify-between px-4 lg:px-6">
        {/* Logo */}
        <div className="flex items-center gap-6">
          <button onClick={() => onNavigate("courses")} className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent">
              <Key className="h-4 w-4 text-accent-foreground" />
            </div>
            <span className="text-lg font-semibold text-foreground">yali</span>
          </button>

          {/* Navigation */}
          <nav className="hidden items-center gap-1 md:flex">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive = currentView === item.id
              return (
                <button
                  key={item.id}
                  onClick={() => onNavigate(item.id)}
                  className={`flex items-center gap-2 rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
                    isActive
                      ? "bg-secondary text-foreground"
                      : "text-muted-foreground hover:bg-secondary/50 hover:text-foreground"
                  }`}
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </button>
              )
            })}
          </nav>
        </div>

        {/* Right Section */}
        <div className="flex items-center gap-3">
          {/* Search */}
          <div className="relative hidden sm:block">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search..."
              className="h-9 w-48 rounded-md border border-input bg-background pl-9 pr-4 text-sm outline-none transition-colors placeholder:text-muted-foreground focus:border-accent focus:ring-1 focus:ring-accent lg:w-64"
            />
          </div>

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="relative flex items-center gap-2 px-2">
                <Avatar className="h-8 w-8">
                  <AvatarImage src="https://github.com/shadcn.png" alt="User" />
                  <AvatarFallback className="bg-accent text-xs text-accent-foreground">{displayInitials}</AvatarFallback>
                </Avatar>
                <span className="hidden text-sm font-medium text-foreground lg:inline">{displayName}</span>
                <ChevronDown className="hidden h-4 w-4 text-muted-foreground lg:inline" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <div className="px-2 py-1.5">
                <p className="text-sm font-medium text-foreground">{displayName}</p>
                <p className="text-xs text-muted-foreground">{displayEmail}</p>
              </div>
              <DropdownMenuSeparator />
              <DropdownMenuItem className="gap-2" onClick={() => onNavigate("profile")}> 
                <User className="h-4 w-4" />
                个人详情
              </DropdownMenuItem>
              <DropdownMenuItem className="gap-2" onClick={() => onNavigate("password")}> 
                <Settings className="h-4 w-4" />
                修改密码
              </DropdownMenuItem>
              <DropdownMenuItem className="gap-2" onClick={() => onNavigate("userLookup")}> 
                <Search className="h-4 w-4" />
                按 ID 查用户
              </DropdownMenuItem>
              <DropdownMenuSeparator />

              {/* Mobile Navigation */}
              <div className="md:hidden">
                {navItems.map((item) => {
                  const Icon = item.icon
                  return (
                    <DropdownMenuItem key={item.id} onClick={() => onNavigate(item.id)} className="gap-2">
                      <Icon className="h-4 w-4" />
                      {item.label}
                    </DropdownMenuItem>
                  )
                })}
                <DropdownMenuSeparator />
              </div>

              <DropdownMenuItem onClick={onLogout} className="gap-2 text-destructive">
                <LogOut className="h-4 w-4" />
                {isLoggingOut ? "Signing Out..." : "Sign Out"}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  )
}

export default function App() {
  const [currentView, setCurrentView] = useState<ViewType>("auth")
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null)
  const [isBootstrapping, setIsBootstrapping] = useState(true)
  const [isLoggingOut, setIsLoggingOut] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      const token = getAuthToken()

      if (!token) {
        if (isMounted) {
          setIsBootstrapping(false)
        }
        return
      }

      try {
        const user = await getCurrentUser()
        if (!isMounted) {
          return
        }
        setCurrentUser(user)
        setIsLoggedIn(true)
        setCurrentView("courses")
      } catch {
        clearAuthToken()
      } finally {
        if (isMounted) {
          setIsBootstrapping(false)
        }
      }
    }

    bootstrap()

    return () => {
      isMounted = false
    }
  }, [])

  const handleLogin = (user: UserResponse) => {
    setCurrentUser(user)
    setIsLoggedIn(true)
    setCurrentView("courses")
  }

  const handleLogout = async () => {
    setIsLoggingOut(true)

    try {
      if (getAuthToken()) {
        await logoutUser()
      }
    } catch {
      // Ignore logout request errors and force local sign-out.
    } finally {
      clearAuthToken()
      setCurrentUser(null)
      setIsLoggedIn(false)
      setCurrentView("auth")
      setIsLoggingOut(false)
    }
  }

  const handleNavigate = (view: ViewType) => {
    setCurrentView(view)
  }

  if (isBootstrapping) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background text-sm text-muted-foreground">
        正在恢复登录状态...
      </div>
    )
  }

  // Show auth view if not logged in
  if (!isLoggedIn) {
    return <AuthView onLogin={handleLogin} />
  }

  // Render the appropriate view based on current state
  const renderView = () => {
    switch (currentView) {
      case "courses":
        return <DocsView />
      case "community":
        return <CommunityView />
      case "admin":
        return <AdminView />
      case "profile":
        return <ProfileView initialUser={currentUser} onProfileUpdated={setCurrentUser} />
      case "password":
        return <PasswordView />
      case "userLookup":
        return <UserLookupView />
      default:
        return <DocsView />
    }
  }

  // For admin view, it has its own layout, so we don't wrap it
  if (currentView === "admin") {
    return (
      <div className="min-h-screen bg-background">
        <GlobalNavbar
          currentView={currentView}
          onNavigate={handleNavigate}
          onLogout={handleLogout}
          currentUser={currentUser}
          isLoggingOut={isLoggingOut}
        />
        <AdminView />
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <GlobalNavbar
        currentView={currentView}
        onNavigate={handleNavigate}
        onLogout={handleLogout}
        currentUser={currentUser}
        isLoggingOut={isLoggingOut}
      />
      {renderView()}
    </div>
  )
}
