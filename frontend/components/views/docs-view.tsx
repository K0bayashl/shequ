"use client"

import { useEffect, useState } from "react"
import {
  ChevronDown,
  ChevronRight,
  BookOpen,
  FileText,
  Code2,
  Terminal,
  Layers,
  Settings,
  Menu,
  Copy,
  Check,
} from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { ScrollArea } from "@/components/ui/scroll-area"
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { getDocsChapters, scaffoldPing, type ScaffoldStatusResponse } from "@/lib/backend-api"

// Chapter navigation data
const defaultChapters = [
  {
    title: "Getting Started",
    icon: BookOpen,
    items: [
      { title: "Introduction", href: "#", active: false },
      { title: "Installation", href: "#", active: true },
      { title: "Quick Start Guide", href: "#", active: false },
    ],
  },
  {
    title: "Core Concepts",
    icon: Layers,
    items: [
      { title: "Architecture Overview", href: "#", active: false },
      { title: "Data Flow", href: "#", active: false },
      { title: "State Management", href: "#", active: false },
      { title: "Error Handling", href: "#", active: false },
    ],
  },
  {
    title: "API Reference",
    icon: Code2,
    items: [
      { title: "Authentication", href: "#", active: false },
      { title: "Endpoints", href: "#", active: false },
      { title: "Webhooks", href: "#", active: false },
    ],
  },
  {
    title: "CLI Tools",
    icon: Terminal,
    items: [
      { title: "Commands", href: "#", active: false },
      { title: "Configuration", href: "#", active: false },
    ],
  },
  {
    title: "Advanced",
    icon: Settings,
    items: [
      { title: "Custom Plugins", href: "#", active: false },
      { title: "Performance", href: "#", active: false },
      { title: "Security", href: "#", active: false },
    ],
  },
]

const chapterIconMap = {
  book: BookOpen,
  layers: Layers,
  code: Code2,
  terminal: Terminal,
  settings: Settings,
} as const

type ChapterConfig = {
  title: string
  icon: (typeof chapterIconMap)[keyof typeof chapterIconMap]
  items: {
    title: string
    href: string
    active: boolean
  }[]
}

function CodeBlock({ code, language }: { code: string; language: string }) {
  const [copied, setCopied] = useState(false)

  const copyToClipboard = () => {
    navigator.clipboard.writeText(code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="group relative my-6 overflow-hidden rounded-lg border border-border bg-[#0a0a0a]">
      <div className="flex items-center justify-between border-b border-border/50 bg-[#111111] px-4 py-2">
        <span className="font-mono text-xs text-muted-foreground">{language}</span>
        <Button
          variant="ghost"
          size="sm"
          className="h-7 gap-1.5 px-2 text-xs text-muted-foreground hover:bg-secondary/50 hover:text-foreground"
          onClick={copyToClipboard}
        >
          {copied ? (
            <>
              <Check className="size-3.5" />
              <span>Copied</span>
            </>
          ) : (
            <>
              <Copy className="size-3.5" />
              <span>Copy</span>
            </>
          )}
        </Button>
      </div>
      <pre className="overflow-x-auto p-4">
        <code className="font-mono text-sm leading-relaxed text-[#e5e5e5]">
          {code.split("\n").map((line, i) => (
            <div key={i} className="table-row">
              <span className="table-cell select-none pr-4 text-right text-muted-foreground/50">
                {i + 1}
              </span>
              <span className="table-cell">
                {line
                  .split(
                    /(\b(?:import|from|export|const|function|return|async|await|if|else)\b|'[^']*'|"[^"]*"|`[^`]*`|\/\/.*$|\{|\}|\(|\)|=>|:|,)/g
                  )
                  .map((part, j) => {
                    if (/^(import|from|export|const|function|return|async|await|if|else)$/.test(part)) {
                      return (
                        <span key={j} className="text-[#c586c0]">
                          {part}
                        </span>
                      )
                    }
                    if (/^['"`].*['"`]$/.test(part)) {
                      return (
                        <span key={j} className="text-[#ce9178]">
                          {part}
                        </span>
                      )
                    }
                    if (/^\/\//.test(part)) {
                      return (
                        <span key={j} className="text-[#6a9955]">
                          {part}
                        </span>
                      )
                    }
                    if (/^[{}()=>:,]$/.test(part)) {
                      return (
                        <span key={j} className="text-[#d4d4d4]">
                          {part}
                        </span>
                      )
                    }
                    return <span key={j}>{part}</span>
                  })}
              </span>
            </div>
          ))}
        </code>
      </pre>
    </div>
  )
}

function ChapterNav({
  chapters,
  className,
}: {
  chapters: ChapterConfig[]
  className?: string
}) {
  const [openSections, setOpenSections] = useState<string[]>(["Getting Started"])

  const toggleSection = (title: string) => {
    setOpenSections((prev) =>
      prev.includes(title) ? prev.filter((t) => t !== title) : [...prev, title]
    )
  }

  return (
    <nav className={cn("space-y-1", className)}>
      {chapters.map((chapter) => {
        const Icon = chapter.icon
        const isOpen = openSections.includes(chapter.title)

        return (
          <Collapsible
            key={chapter.title}
            open={isOpen}
            onOpenChange={() => toggleSection(chapter.title)}
          >
            <CollapsibleTrigger className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-foreground transition-colors hover:bg-secondary/80">
              <Icon className="size-4 text-muted-foreground" />
              <span className="flex-1 text-left">{chapter.title}</span>
              {isOpen ? (
                <ChevronDown className="size-4 text-muted-foreground" />
              ) : (
                <ChevronRight className="size-4 text-muted-foreground" />
              )}
            </CollapsibleTrigger>
            <CollapsibleContent className="pl-4">
              <div className="ml-2 border-l border-border py-1">
                {chapter.items.map((item) => (
                  <button
                    key={item.title}
                    className={cn(
                      "flex w-full items-center gap-2 rounded-md px-3 py-1.5 text-sm transition-colors",
                      item.active
                        ? "bg-accent/10 font-medium text-accent"
                        : "text-muted-foreground hover:bg-secondary/50 hover:text-foreground"
                    )}
                  >
                    <FileText className="size-3.5" />
                    {item.title}
                  </button>
                ))}
              </div>
            </CollapsibleContent>
          </Collapsible>
        )
      })}
    </nav>
  )
}

export function DocsView() {
  const [statusLoading, setStatusLoading] = useState(true)
  const [statusError, setStatusError] = useState<string | null>(null)
  const [backendStatus, setBackendStatus] = useState<ScaffoldStatusResponse | null>(null)
  const [chapters, setChapters] = useState<ChapterConfig[]>(defaultChapters)

  const sampleCode = `import { createClient } from '@yali/sdk'

// Initialize the yali client
const client = createClient({
  apiKey: process.env.YALI_API_KEY,
  environment: 'production'
})

// Fetch user data
async function getUser(userId: string) {
  const user = await client.users.get(userId)
  return user
}

// Create a new project
export async function createProject(name: string) {
  const project = await client.projects.create({
    name,
    visibility: 'private'
  })
  
  return project
}`

    useEffect(() => {
      let mounted = true

      const loadScaffoldStatus = async () => {
        try {
          const response = await scaffoldPing()
          if (!mounted) {
            return
          }
          setBackendStatus(response)
        } catch (error) {
          if (!mounted) {
            return
          }
          const message = error instanceof Error ? error.message : "后端状态拉取失败"
          setStatusError(message)
        } finally {
          if (mounted) {
            setStatusLoading(false)
          }
        }
      }

      loadScaffoldStatus()

      const loadDocsChapters = async () => {
        try {
          const response = await getDocsChapters()
          if (!mounted) {
            return
          }

          const mappedChapters: ChapterConfig[] = response.chapters.map((chapter) => {
            const icon = chapterIconMap[chapter.iconKey as keyof typeof chapterIconMap] || BookOpen
            return {
              title: chapter.title,
              icon,
              items: chapter.items,
            }
          })

          setChapters(mappedChapters)
        } catch {
          // Keep default static chapters when backend chapters are unavailable.
        }
      }

      loadDocsChapters()

      return () => {
        mounted = false
      }
    }, [])

  return (
    <div className="flex min-h-screen flex-col bg-background">
      {/* Top Bar */}
      <header className="sticky top-0 z-50 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="flex h-14 items-center gap-4 px-4 lg:px-6">
          {/* Mobile menu */}
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="lg:hidden">
                <Menu className="size-5" />
                <span className="sr-only">Toggle menu</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-72 p-0">
              <div className="flex h-14 items-center border-b border-border px-4">
                <div className="flex items-center gap-2">
                  <div className="flex size-7 items-center justify-center rounded-md bg-foreground">
                    <span className="text-xs font-bold text-background">Y</span>
                  </div>
                  <span className="font-semibold">yali docs</span>
                </div>
              </div>
              <ScrollArea className="h-[calc(100vh-3.5rem)]">
                <div className="p-4">
                  <ChapterNav chapters={chapters} />
                </div>
              </ScrollArea>
            </SheetContent>
          </Sheet>

          <div className="hidden items-center gap-2 lg:flex">
            <div className="flex size-7 items-center justify-center rounded-md bg-foreground">
              <span className="text-xs font-bold text-background">Y</span>
            </div>
            <span className="font-semibold">yali docs</span>
          </div>

          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <span>/</span>
            <span>Getting Started</span>
            <span>/</span>
            <span className="text-foreground">Installation</span>
          </div>

          <div className="flex-1" />

          <Avatar className="size-8">
            <AvatarImage src="https://github.com/shadcn.png" alt="User" />
            <AvatarFallback className="bg-secondary text-secondary-foreground">JD</AvatarFallback>
          </Avatar>
        </div>
      </header>

      <div className="flex flex-1">
        {/* Left Sidebar */}
        <aside className="hidden w-64 shrink-0 border-r border-border lg:block">
          <ScrollArea className="h-[calc(100vh-3.5rem)]">
            <div className="p-4">
              <ChapterNav chapters={chapters} />
            </div>
          </ScrollArea>
        </aside>

        {/* Main Content */}
        <main className="flex-1 overflow-auto">
          <div className="mx-auto max-w-3xl px-6 py-10 lg:px-8 lg:py-12">
            {/* Article Header */}
            <header className="mb-10">
              <h1 className="text-balance text-4xl font-bold tracking-tight text-foreground lg:text-5xl">
                Installation
              </h1>
              <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
                <div className="flex items-center gap-2">
                  <Avatar className="size-6">
                    <AvatarImage src="https://github.com/shadcn.png" alt="Author" />
                    <AvatarFallback className="text-xs">JD</AvatarFallback>
                  </Avatar>
                  <span>John Developer</span>
                </div>
                <span className="hidden sm:inline">•</span>
                <time dateTime="2024-03-15">March 15, 2024</time>
                <span className="hidden sm:inline">•</span>
                <span>5 min read</span>
              </div>

              <div className="mt-4 rounded-md border border-border bg-muted/30 px-3 py-2 text-sm">
                <p className="font-medium text-foreground">Backend Sync</p>
                {statusLoading && <p className="mt-1 text-muted-foreground">正在检查后端连接...</p>}
                {!statusLoading && statusError && (
                  <p className="mt-1 text-destructive">后端不可用：{statusError}</p>
                )}
                {!statusLoading && backendStatus && (
                  <div className="mt-1 flex flex-wrap items-center gap-2 text-muted-foreground">
                    <span>{backendStatus.application}</span>
                    <span>profiles: {backendStatus.activeProfiles.join(",") || "local"}</span>
                    <span>db: {backendStatus.databaseEnabled ? "on" : "off"}</span>
                    <span>jwt: {backendStatus.jwtEnabled ? "on" : "off"}</span>
                  </div>
                )}
              </div>
            </header>

            {/* Article Content */}
            <article className="prose prose-neutral dark:prose-invert max-w-none">
              <p className="text-lg leading-relaxed text-muted-foreground">
                This guide will walk you through setting up the yali SDK in your project. We support
                multiple package managers and provide first-class TypeScript support out of the box.
              </p>

              <h2 className="mt-10 scroll-m-20 border-b border-border pb-2 text-2xl font-semibold tracking-tight text-foreground">
                Prerequisites
              </h2>

              <p className="mt-4 leading-7 text-foreground/90">
                Before you begin, make sure you have the following installed on your system:
              </p>

              <ul className="ml-6 mt-4 list-disc space-y-2 text-foreground/90 [&>li]:pl-1">
                <li>Node.js 18.0 or later</li>
                <li>npm, yarn, or pnpm package manager</li>
                <li>A yali account with an active API key</li>
              </ul>

              <h2 className="mt-10 scroll-m-20 border-b border-border pb-2 text-2xl font-semibold tracking-tight text-foreground">
                Quick Installation
              </h2>

              <p className="mt-4 leading-7 text-foreground/90">
                Install the yali SDK using your preferred package manager:
              </p>

              <CodeBlock code="npm install @yali/sdk" language="bash" />

              <h2 className="mt-10 scroll-m-20 border-b border-border pb-2 text-2xl font-semibold tracking-tight text-foreground">
                Basic Setup
              </h2>

              <p className="mt-4 leading-7 text-foreground/90">
                Once installed, you can initialize the client and start making API calls. Here is a
                complete example showing the basic setup:
              </p>

              <CodeBlock code={sampleCode} language="typescript" />

              {/* Blockquote */}
              <blockquote className="mt-8 border-l-4 border-accent pl-6 italic text-muted-foreground">
                <p className="leading-relaxed">
                  {
                    '"The yali SDK is designed with developer experience as the top priority. Every API is intuitive, well-documented, and built for modern TypeScript workflows."'
                  }
                </p>
                <footer className="mt-2 text-sm font-medium text-foreground/70">
                  — yali Engineering Team
                </footer>
              </blockquote>

              <div className="mt-10 rounded-lg border border-accent/20 bg-accent/5 p-6">
                <h3 className="text-lg font-semibold text-foreground">Next Steps</h3>
                <p className="mt-2 text-sm text-muted-foreground">
                  Now that you have the SDK installed, check out the Quick Start Guide to build your
                  first integration, or dive into the Core Concepts to understand the architecture.
                </p>
                <div className="mt-4 flex flex-wrap gap-3">
                  <Button variant="default" size="sm" className="gap-2">
                    <BookOpen className="size-4" />
                    Quick Start Guide
                  </Button>
                  <Button variant="outline" size="sm" className="gap-2">
                    <Layers className="size-4" />
                    Core Concepts
                  </Button>
                </div>
              </div>
            </article>

            {/* Footer Navigation */}
            <nav className="mt-16 flex items-center justify-between border-t border-border pt-8">
              <button className="group flex flex-col gap-1 text-sm text-muted-foreground transition-colors hover:text-foreground">
                <span className="text-xs uppercase tracking-wider">Previous</span>
                <span className="flex items-center gap-1 font-medium">
                  <ChevronRight className="size-4 rotate-180 transition-transform group-hover:-translate-x-0.5" />
                  Introduction
                </span>
              </button>
              <button className="group flex flex-col items-end gap-1 text-sm text-muted-foreground transition-colors hover:text-foreground">
                <span className="text-xs uppercase tracking-wider">Next</span>
                <span className="flex items-center gap-1 font-medium">
                  Quick Start Guide
                  <ChevronRight className="size-4 transition-transform group-hover:translate-x-0.5" />
                </span>
              </button>
            </nav>
          </div>
        </main>
      </div>
    </div>
  )
}
