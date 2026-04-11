"use client"

import { useCallback, useEffect, useState } from "react"
import {
  LayoutDashboard,
  Users,
  BookOpen,
  Shield,
  Key,
  Copy,
  Check,
  Plus,
  Search,
  ChevronDown,
  LogOut,
  Settings,
  Menu,
  X,
  MoreHorizontal,
  Trash2,
  RefreshCw,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { getAdminCdks, type AdminCdkItem } from "@/lib/backend-api"

const navItems = [
  { id: "overview", label: "Overview", icon: LayoutDashboard },
  { id: "users", label: "Users", icon: Users },
  { id: "courses", label: "Courses", icon: BookOpen },
  { id: "moderation", label: "Content Moderation", icon: Shield },
  { id: "cdk", label: "CDK Management", icon: Key },
]

const mockCDKs = [
  {
    id: 1,
    key: "YALI-2024-AXKJ-8F92",
    status: "used",
    usedBy: "Chen Wei",
    usedByEmail: "chen.wei@example.com",
    usedAt: "2024-03-15",
    createdAt: "2024-03-01",
  },
  {
    id: 2,
    key: "YALI-2024-BMNP-3D47",
    status: "used",
    usedBy: "Li Ming",
    usedByEmail: "li.ming@example.com",
    usedAt: "2024-03-14",
    createdAt: "2024-03-01",
  },
  {
    id: 3,
    key: "YALI-2024-CQRS-6H21",
    status: "unused",
    usedBy: null,
    usedByEmail: null,
    usedAt: null,
    createdAt: "2024-03-10",
  },
  {
    id: 4,
    key: "YALI-2024-DTUV-9K58",
    status: "unused",
    usedBy: null,
    usedByEmail: null,
    usedAt: null,
    createdAt: "2024-03-10",
  },
  {
    id: 5,
    key: "YALI-2024-EWXY-2L84",
    status: "used",
    usedBy: "Wang Fang",
    usedByEmail: "wang.fang@example.com",
    usedAt: "2024-03-12",
    createdAt: "2024-03-05",
  },
  {
    id: 6,
    key: "YALI-2024-FZAB-5M37",
    status: "expired",
    usedBy: null,
    usedByEmail: null,
    usedAt: null,
    createdAt: "2024-01-01",
  },
  {
    id: 7,
    key: "YALI-2024-GCDE-7N69",
    status: "unused",
    usedBy: null,
    usedByEmail: null,
    usedAt: null,
    createdAt: "2024-03-12",
  },
  {
    id: 8,
    key: "YALI-2024-HFGH-1P42",
    status: "used",
    usedBy: "Zhang Lei",
    usedByEmail: "zhang.lei@example.com",
    usedAt: "2024-03-13",
    createdAt: "2024-03-08",
  },
]

export function AdminView() {
  const [activeNav, setActiveNav] = useState("cdk")
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [copiedKey, setCopiedKey] = useState<string | null>(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<"all" | "used" | "unused" | "expired">("all")
  const [generateDialogOpen, setGenerateDialogOpen] = useState(false)
  const [generateCount, setGenerateCount] = useState("5")
  const [cdkRows, setCdkRows] = useState<AdminCdkItem[]>(mockCDKs)
  const [isSyncing, setIsSyncing] = useState(false)
  const [syncError, setSyncError] = useState<string | null>(null)
  const [stats, setStats] = useState({
    totalUsers: 1247,
    activeMembers: 892,
    remainingCDKs: mockCDKs.filter((c) => c.status === "unused").length,
  })

  const syncAdminData = useCallback(async (search = searchQuery, status = statusFilter) => {
    setIsSyncing(true)
    setSyncError(null)

    try {
      const response = await getAdminCdks(search, status)
      setCdkRows(response.cdks)
      setStats({
        totalUsers: response.totalUsers,
        activeMembers: response.activeMembers,
        remainingCDKs: response.remainingCdks,
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : "CDK 数据同步失败"
      setSyncError(message)
    } finally {
      setIsSyncing(false)
    }
  }, [searchQuery, statusFilter])

  useEffect(() => {
    const currentSearch = searchQuery
    const currentStatus = statusFilter

    const timer = setTimeout(() => {
      void syncAdminData(currentSearch, currentStatus)
    }, 200)

    return () => {
      clearTimeout(timer)
    }
  }, [searchQuery, statusFilter, syncAdminData])

  const copyToClipboard = (key: string) => {
    navigator.clipboard.writeText(key)
    setCopiedKey(key)
    setTimeout(() => setCopiedKey(null), 2000)
  }

  const filteredCDKs = cdkRows

  return (
    <div className="flex min-h-screen bg-background">
      {/* Mobile Sidebar Overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-foreground/20 backdrop-blur-sm lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-border bg-card transition-transform duration-200 lg:static lg:translate-x-0 ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {/* Sidebar Header */}
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

        {/* Navigation */}
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

        {/* Sidebar Footer */}
        <div className="border-t border-border p-4">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button className="flex w-full items-center gap-3 rounded-lg px-3 py-2 hover:bg-secondary">
                <Avatar className="h-8 w-8">
                  <AvatarImage src="/placeholder.svg" />
                  <AvatarFallback className="bg-accent text-xs text-accent-foreground">AD</AvatarFallback>
                </Avatar>
                <div className="flex-1 text-left">
                  <p className="text-sm font-medium text-foreground">Admin</p>
                  <p className="text-xs text-muted-foreground">admin@yali.dev</p>
                </div>
                <ChevronDown className="h-4 w-4 text-muted-foreground" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuItem>
                <Settings className="mr-2 h-4 w-4" />
                Settings
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem className="text-destructive">
                <LogOut className="mr-2 h-4 w-4" />
                Log out
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex flex-1 flex-col">
        {/* Top Header */}
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border bg-card/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-card/60 lg:px-8">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setSidebarOpen(true)}>
              <Menu className="h-5 w-5" />
            </Button>
            <div>
              <h1 className="text-lg font-semibold text-foreground">CDK Management</h1>
              <p className="text-sm text-muted-foreground">Manage license keys and activations</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Button variant="outline" size="sm" className="hidden sm:flex" onClick={() => void syncAdminData()}>
              <RefreshCw className="mr-2 h-4 w-4" />
              {isSyncing ? "Syncing..." : "Refresh"}
            </Button>
          </div>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 p-4 lg:p-8">
          <div className="mb-4 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            {isSyncing && <span>正在同步管理数据...</span>}
            {!isSyncing && !syncError && <span>已接入后端管理接口</span>}
            {!isSyncing && syncError && <span className="text-destructive">同步失败：{syncError}</span>}
          </div>

          {/* Metric Cards */}
          <div className="mb-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <Card className="border-border">
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">Total Users</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-foreground">{stats.totalUsers.toLocaleString()}</div>
                <p className="mt-1 text-xs text-muted-foreground">
                  <span className="text-accent">+12.5%</span> from last month
                </p>
              </CardContent>
            </Card>

            <Card className="border-border">
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">Active Members</CardTitle>
                <LayoutDashboard className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-foreground">{stats.activeMembers.toLocaleString()}</div>
                <p className="mt-1 text-xs text-muted-foreground">
                  <span className="text-accent">71.5%</span> activation rate
                </p>
              </CardContent>
            </Card>

            <Card className="border-border sm:col-span-2 lg:col-span-1">
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">Remaining CDKs</CardTitle>
                <Key className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-foreground">{stats.remainingCDKs}</div>
                <p className="mt-1 text-xs text-muted-foreground">Available for distribution</p>
              </CardContent>
            </Card>
          </div>

          {/* CDK Table Section */}
          <Card className="border-border">
            <CardHeader className="border-b border-border">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <CardTitle className="text-foreground">License Keys</CardTitle>
                  <p className="mt-1 text-sm text-muted-foreground">A list of all generated CDK license keys</p>
                </div>
                <Dialog open={generateDialogOpen} onOpenChange={setGenerateDialogOpen}>
                  <DialogTrigger asChild>
                    <Button className="bg-accent text-accent-foreground hover:bg-accent/90">
                      <Plus className="mr-2 h-4 w-4" />
                      Generate New CDKs
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Generate New CDKs</DialogTitle>
                      <DialogDescription>Create new license keys for distribution to users.</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                      <div className="grid gap-2">
                        <Label htmlFor="count">Number of CDKs</Label>
                        <Select value={generateCount} onValueChange={setGenerateCount}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select count" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="5">5 keys</SelectItem>
                            <SelectItem value="10">10 keys</SelectItem>
                            <SelectItem value="25">25 keys</SelectItem>
                            <SelectItem value="50">50 keys</SelectItem>
                            <SelectItem value="100">100 keys</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="grid gap-2">
                        <Label htmlFor="prefix">Key Prefix</Label>
                        <Input id="prefix" defaultValue="YALI-2024" />
                      </div>
                    </div>
                    <DialogFooter>
                      <Button variant="outline" onClick={() => setGenerateDialogOpen(false)}>
                        Cancel
                      </Button>
                      <Button
                        className="bg-accent text-accent-foreground hover:bg-accent/90"
                        onClick={() => setGenerateDialogOpen(false)}
                      >
                        Generate {generateCount} CDKs
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </div>
            </CardHeader>

            {/* Filters */}
            <div className="flex flex-col gap-4 border-b border-border p-4 sm:flex-row sm:items-center">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search by key, user, or email..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-9"
                />
              </div>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-full sm:w-40">
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="unused">Unused</SelectItem>
                  <SelectItem value="used">Used</SelectItem>
                  <SelectItem value="expired">Expired</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Table */}
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow className="border-border hover:bg-transparent">
                    <TableHead className="text-muted-foreground">License Key</TableHead>
                    <TableHead className="text-muted-foreground">Status</TableHead>
                    <TableHead className="text-muted-foreground">Used By</TableHead>
                    <TableHead className="text-muted-foreground">Used At</TableHead>
                    <TableHead className="text-muted-foreground">Created</TableHead>
                    <TableHead className="text-right text-muted-foreground">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredCDKs.map((cdk) => (
                    <TableRow key={cdk.id} className="border-border">
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <code className="rounded bg-secondary px-2 py-1 font-mono text-sm text-foreground">
                            {cdk.key}
                          </code>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-7 w-7"
                            onClick={() => copyToClipboard(cdk.key)}
                          >
                            {copiedKey === cdk.key ? (
                              <Check className="h-3.5 w-3.5 text-accent" />
                            ) : (
                              <Copy className="h-3.5 w-3.5 text-muted-foreground" />
                            )}
                          </Button>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            cdk.status === "used"
                              ? "default"
                              : cdk.status === "unused"
                                ? "secondary"
                                : "destructive"
                          }
                          className={
                            cdk.status === "used"
                              ? "bg-accent/10 text-accent hover:bg-accent/20"
                              : cdk.status === "unused"
                                ? "bg-secondary text-foreground"
                                : ""
                          }
                        >
                          {cdk.status.charAt(0).toUpperCase() + cdk.status.slice(1)}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {cdk.usedBy ? (
                          <div className="flex items-center gap-2">
                            <Avatar className="h-6 w-6">
                              <AvatarFallback className="bg-secondary text-xs text-foreground">
                                {cdk.usedBy
                                  .split(" ")
                                  .map((n) => n[0])
                                  .join("")}
                              </AvatarFallback>
                            </Avatar>
                            <div>
                              <p className="text-sm font-medium text-foreground">{cdk.usedBy}</p>
                              <p className="text-xs text-muted-foreground">{cdk.usedByEmail}</p>
                            </div>
                          </div>
                        ) : (
                          <span className="text-sm text-muted-foreground">—</span>
                        )}
                      </TableCell>
                      <TableCell>
                        <span className="text-sm text-foreground">{cdk.usedAt || "—"}</span>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm text-muted-foreground">{cdk.createdAt}</span>
                      </TableCell>
                      <TableCell className="text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="h-8 w-8">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => copyToClipboard(cdk.key)}>
                              <Copy className="mr-2 h-4 w-4" />
                              Copy Key
                            </DropdownMenuItem>
                            {cdk.status === "unused" && (
                              <DropdownMenuItem className="text-destructive">
                                <Trash2 className="mr-2 h-4 w-4" />
                                Revoke
                              </DropdownMenuItem>
                            )}
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            {/* Pagination */}
            <div className="flex items-center justify-between border-t border-border px-4 py-4">
              <p className="text-sm text-muted-foreground">Showing {filteredCDKs.length} results</p>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" disabled>
                  Previous
                </Button>
                <Button variant="outline" size="sm" disabled>
                  Next
                </Button>
              </div>
            </div>
          </Card>
        </main>
      </div>
    </div>
  )
}
