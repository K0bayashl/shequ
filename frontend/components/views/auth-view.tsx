"use client"

import { FormEvent, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field"
import { InputGroup, InputGroupInput } from "@/components/ui/input-group"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { Lock, Mail, User, Key, Sparkles, Info } from "lucide-react"
import {
  loginUser,
  registerUser,
  setAuthToken,
  type RegisterRequest,
  type UserResponse,
} from "@/lib/backend-api"

interface AuthViewProps {
  onLogin?: (user: UserResponse) => void
}

export function AuthView({ onLogin }: AuthViewProps) {
  const [activeTab, setActiveTab] = useState("login")
  const [submittingTab, setSubmittingTab] = useState<"login" | "activate" | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [loginForm, setLoginForm] = useState({
    email: "",
    password: "",
  })
  const [activateForm, setActivateForm] = useState<RegisterRequest>({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    cdkCode: "",
  })

  const handleSignIn = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setErrorMessage(null)
    setSuccessMessage(null)
    setSubmittingTab("login")

    try {
      const response = await loginUser({
        email: loginForm.email,
        password: loginForm.password,
      })

      setAuthToken(response.token)
      setSuccessMessage("登录成功，正在进入社区...")
      onLogin?.(response.user)
    } catch (error) {
      const message = error instanceof Error ? error.message : "登录失败，请稍后重试"
      setErrorMessage(message)
    } finally {
      setSubmittingTab(null)
    }
  }

  const handleActivate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setErrorMessage(null)
    setSuccessMessage(null)

    if (activateForm.password !== activateForm.confirmPassword) {
      setErrorMessage("两次输入的密码不一致")
      return
    }

    setSubmittingTab("activate")

    try {
      await registerUser(activateForm)

      const response = await loginUser({
        email: activateForm.email,
        password: activateForm.password,
      })

      setAuthToken(response.token)
      setSuccessMessage("激活成功，正在进入社区...")
      onLogin?.(response.user)
    } catch (error) {
      const message = error instanceof Error ? error.message : "激活失败，请稍后重试"
      setErrorMessage(message)
    } finally {
      setSubmittingTab(null)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="mb-8 text-center">
          <div className="mb-4 flex items-center justify-center gap-2">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent">
              <Key className="h-5 w-5 text-accent-foreground" />
            </div>
            <h1 className="text-2xl font-semibold tracking-tight text-foreground">{"yali"}</h1>
          </div>
          <p className="text-balance text-sm text-muted-foreground">
            {"Exclusive access for premium developers"}
          </p>
        </div>

        {/* Auth Card */}
        <Card className="border-border shadow-sm">
          <CardHeader className="space-y-1 pb-4">
            <CardTitle className="text-xl font-semibold">{"Welcome back"}</CardTitle>
            <CardDescription>
              {activeTab === "login"
                ? "Sign in to your account to continue"
                : "Activate your premium membership"}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {errorMessage && (
              <p className="mb-4 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                {errorMessage}
              </p>
            )}
            {successMessage && (
              <p className="mb-4 rounded-md border border-emerald-300/40 bg-emerald-500/10 px-3 py-2 text-sm text-emerald-600">
                {successMessage}
              </p>
            )}
            <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="login">{"Login"}</TabsTrigger>
                <TabsTrigger value="activate">{"Activate"}</TabsTrigger>
              </TabsList>

              {/* Login Tab */}
              <TabsContent value="login" className="mt-6 space-y-4">
                <form className="space-y-4" onSubmit={handleSignIn}>
                  <FieldGroup>
                    <Field>
                      <FieldLabel htmlFor="login-email">{"Email"}</FieldLabel>
                      <InputGroup>
                        <Mail className="h-4 w-4 text-muted-foreground" />
                        <InputGroupInput
                          id="login-email"
                          type="email"
                          placeholder="you@example.com"
                          autoComplete="email"
                          value={loginForm.email}
                          onChange={(event) =>
                            setLoginForm((prev) => ({
                              ...prev,
                              email: event.target.value,
                            }))
                          }
                          required
                        />
                      </InputGroup>
                    </Field>

                    <Field>
                      <FieldLabel htmlFor="login-password">{"Password"}</FieldLabel>
                      <InputGroup>
                        <Lock className="h-4 w-4 text-muted-foreground" />
                        <InputGroupInput
                          id="login-password"
                          type="password"
                          placeholder="Enter your password"
                          autoComplete="current-password"
                          value={loginForm.password}
                          onChange={(event) =>
                            setLoginForm((prev) => ({
                              ...prev,
                              password: event.target.value,
                            }))
                          }
                          required
                        />
                      </InputGroup>
                    </Field>
                  </FieldGroup>

                  <Button className="w-full" size="lg" type="submit" disabled={submittingTab !== null}>
                    {submittingTab === "login" ? "Signing In..." : "Sign In"}
                  </Button>

                  <div className="text-center">
                    <button className="text-sm text-muted-foreground transition-colors hover:text-foreground" type="button">
                      {"Forgot password?"}
                    </button>
                  </div>
                </form>
              </TabsContent>

              {/* Activate Tab */}
              <TabsContent value="activate" className="mt-6 space-y-4">
                <form className="space-y-4" onSubmit={handleActivate}>
                  <FieldGroup>
                    <Field>
                      <FieldLabel htmlFor="activate-username">{"Username"}</FieldLabel>
                      <InputGroup>
                        <User className="h-4 w-4 text-muted-foreground" />
                        <InputGroupInput
                          id="activate-username"
                          type="text"
                          placeholder="johndoe"
                          autoComplete="username"
                          value={activateForm.username}
                          onChange={(event) =>
                            setActivateForm((prev) => ({
                              ...prev,
                              username: event.target.value,
                            }))
                          }
                          required
                        />
                      </InputGroup>
                    </Field>

                    <Field>
                      <FieldLabel htmlFor="activate-email">{"Email"}</FieldLabel>
                      <InputGroup>
                        <Mail className="h-4 w-4 text-muted-foreground" />
                        <InputGroupInput
                          id="activate-email"
                          type="email"
                          placeholder="you@example.com"
                          autoComplete="email"
                          value={activateForm.email}
                          onChange={(event) =>
                            setActivateForm((prev) => ({
                              ...prev,
                              email: event.target.value,
                            }))
                          }
                          required
                        />
                      </InputGroup>
                    </Field>

                    <Field>
                      <FieldLabel htmlFor="activate-password">{"Password"}</FieldLabel>
                      <InputGroup>
                        <Lock className="h-4 w-4 text-muted-foreground" />
                        <InputGroupInput
                          id="activate-password"
                          type="password"
                          placeholder="Create a secure password"
                          autoComplete="new-password"
                          value={activateForm.password}
                          onChange={(event) =>
                            setActivateForm((prev) => ({
                              ...prev,
                              password: event.target.value,
                            }))
                          }
                          required
                        />
                      </InputGroup>
                    </Field>

                    <Field>
                      <FieldLabel htmlFor="activate-confirm-password">{"Confirm Password"}</FieldLabel>
                      <InputGroup>
                        <Lock className="h-4 w-4 text-muted-foreground" />
                        <InputGroupInput
                          id="activate-confirm-password"
                          type="password"
                          placeholder="Confirm your password"
                          autoComplete="new-password"
                          value={activateForm.confirmPassword}
                          onChange={(event) =>
                            setActivateForm((prev) => ({
                              ...prev,
                              confirmPassword: event.target.value,
                            }))
                          }
                          required
                        />
                      </InputGroup>
                    </Field>

                    {/* CDK License Key - Prominent Field */}
                    <Field>
                      <div className="mb-2 flex items-center justify-between">
                        <FieldLabel htmlFor="activate-license" className="flex items-center gap-2">
                          <span>{"yali License Key"}</span>
                          <TooltipProvider>
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <Info className="h-3.5 w-3.5 text-muted-foreground" />
                              </TooltipTrigger>
                              <TooltipContent>
                                <p className="text-xs">{"Your unique activation code for lifetime access"}</p>
                              </TooltipContent>
                            </Tooltip>
                          </TooltipProvider>
                        </FieldLabel>
                        <Badge variant="outline" className="gap-1 border-accent/50 bg-accent/10 text-accent">
                          <Sparkles className="h-3 w-3" />
                          {"Unlock Lifetime Access"}
                        </Badge>
                      </div>
                      <InputGroup>
                        <Key className="h-4 w-4 text-accent" />
                        <InputGroupInput
                          id="activate-license"
                          type="text"
                          placeholder="XXXX-XXXX-XXXX-XXXX"
                          className="border-accent/50 font-mono tracking-wider focus-visible:ring-accent"
                          autoComplete="off"
                          value={activateForm.cdkCode}
                          onChange={(event) =>
                            setActivateForm((prev) => ({
                              ...prev,
                              cdkCode: event.target.value,
                            }))
                          }
                          required
                        />
                      </InputGroup>
                      <p className="mt-1.5 text-xs text-muted-foreground">
                        {"Enter the 16-character code from your invitation"}
                      </p>
                    </Field>
                  </FieldGroup>

                  <Button className="w-full bg-accent hover:bg-accent/90" size="lg" type="submit" disabled={submittingTab !== null}>
                    <Sparkles className="mr-2 h-4 w-4" />
                    {submittingTab === "activate" ? "Activating..." : "Activate Account"}
                  </Button>

                  <div className="text-center">
                    <p className="text-xs text-muted-foreground">
                      {"By activating, you agree to our "}
                      <button className="text-accent hover:underline" type="button">{"Terms"}</button>
                      {" and "}
                      <button className="text-accent hover:underline" type="button">{"Privacy Policy"}</button>
                    </p>
                  </div>
                </form>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        {/* Footer */}
        <div className="mt-8 text-center">
          <p className="text-xs text-muted-foreground">
            {activeTab === "login" ? "Don't have an account? " : "Already have an account? "}
            <button
              onClick={() => setActiveTab(activeTab === "login" ? "activate" : "login")}
              className="font-medium text-accent hover:underline"
            >
              {activeTab === "login" ? "Request invitation" : "Sign in"}
            </button>
          </p>
        </div>
      </div>
    </div>
  )
}
