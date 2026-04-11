package com.community.mvp.backend.interfaces.rest.user;

import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.infrastructure.security.CurrentUserContext;
import com.community.mvp.backend.application.user.command.ChangePasswordCommand;
import com.community.mvp.backend.application.user.command.LoginUserCommand;
import com.community.mvp.backend.application.user.command.RegisterUserCommand;
import com.community.mvp.backend.application.user.query.GetProfileQuery;
import com.community.mvp.backend.application.user.query.GetViewerProfileQuery;
import com.community.mvp.backend.application.user.service.AuthenticationResult;
import com.community.mvp.backend.application.user.service.PasswordChangeResult;
import com.community.mvp.backend.application.user.service.RegistrationResult;
import com.community.mvp.backend.application.user.service.UserModuleService;
import com.community.mvp.backend.interfaces.rest.user.dto.ChangePasswordRequest;
import com.community.mvp.backend.interfaces.rest.user.dto.LoginRequest;
import com.community.mvp.backend.interfaces.rest.user.dto.LoginResponse;
import com.community.mvp.backend.interfaces.rest.user.dto.LogoutResponse;
import com.community.mvp.backend.interfaces.rest.user.dto.RegisterRequest;
import com.community.mvp.backend.interfaces.rest.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模块 REST 接口。
 * <p>提供注册、登录、登出、个人信息和密码修改能力。</p>
 */
@RestController
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
@RequestMapping("/api/users")
public class UserController {

    private final UserModuleService userModuleService;
    private final CurrentUserContext currentUserContext;

    /**
     * 构造用户控制器。
     *
     * @param userModuleService 用户应用服务
     * @param currentUserContext 当前用户上下文
     */
    public UserController(UserModuleService userModuleService, CurrentUserContext currentUserContext) {
        this.userModuleService = userModuleService;
        this.currentUserContext = currentUserContext;
    }

    /**
     * 用户注册。
     *
     * @param request 注册请求
     * @return 注册后的用户信息
     */
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegistrationResult result = userModuleService.register(
            new RegisterUserCommand(
                request.username(),
                request.email(),
                request.password(),
                request.confirmPassword(),
                request.cdkCode()
            )
        );
        return ApiResponse.success(toResponse(result.user()));
    }

    /**
     * 用户登录并签发 JWT。
     *
     * @param request 登录请求
     * @return token 与当前用户信息
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = userModuleService.login(new LoginUserCommand(request.email(), request.password()));
        return ApiResponse.success(new LoginResponse(result.token(), toResponse(result.user())));
    }

    /**
     * 用户登出。
     *
     * @return 登出状态
     */
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout() {
        userModuleService.logout();
        return ApiResponse.success(new LogoutResponse(true));
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前用户资料
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        Long currentUserId = currentUserId();
        return ApiResponse.success(toResponse(userModuleService.getProfile(new GetProfileQuery(currentUserId))));
    }

    /**
     * 按用户 ID 查询公开资料。
     *
     * @param id 目标用户 ID
     * @return 目标用户资料
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> profile(@PathVariable Long id) {
        Long currentUserId = currentUserId();
        return ApiResponse.success(toResponse(userModuleService.getProfileForViewer(new GetViewerProfileQuery(currentUserId, id))));
    }

    /**
     * 修改当前登录用户密码。
     *
     * @param request 修改密码请求
     * @return 更新后的用户资料
     */
    @PostMapping("/password/change")
    public ApiResponse<UserResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long currentUserId = currentUserId();
        PasswordChangeResult result = new PasswordChangeResult(
            userModuleService.changePassword(
                new ChangePasswordCommand(
                    currentUserId,
                    request.oldPassword(),
                    request.newPassword(),
                    request.confirmNewPassword()
                )
            )
        );
        return ApiResponse.success(toResponse(result.user()));
    }

    /**
     * 获取当前认证用户 ID。
     *
     * @return 当前用户 ID
     */
    private Long currentUserId() {
        return currentUserContext.currentUser()
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication is required."))
            .userId();
    }

    /**
     * 将领域用户资料映射为接口层响应对象。
     *
     * @param profile 领域用户资料
     * @return 接口响应用户对象
     */
    private UserResponse toResponse(com.community.mvp.backend.domain.user.model.UserProfile profile) {
        return new UserResponse(
            profile.id(),
            profile.username(),
            profile.email(),
            profile.avatar(),
            profile.role(),
            profile.status(),
            profile.createdAt()
        );
    }
}

