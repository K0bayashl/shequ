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

@RestController
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
@RequestMapping("/api/users")
public class UserController {

    private final UserModuleService userModuleService;
    private final CurrentUserContext currentUserContext;

    public UserController(UserModuleService userModuleService, CurrentUserContext currentUserContext) {
        this.userModuleService = userModuleService;
        this.currentUserContext = currentUserContext;
    }

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

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = userModuleService.login(new LoginUserCommand(request.email(), request.password()));
        return ApiResponse.success(new LoginResponse(result.token(), toResponse(result.user())));
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout() {
        userModuleService.logout();
        return ApiResponse.success(new LogoutResponse(true));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        Long currentUserId = currentUserId();
        return ApiResponse.success(toResponse(userModuleService.getProfile(new GetProfileQuery(currentUserId))));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> profile(@PathVariable Long id) {
        Long currentUserId = currentUserId();
        return ApiResponse.success(toResponse(userModuleService.getProfileForViewer(new GetViewerProfileQuery(currentUserId, id))));
    }

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

    private Long currentUserId() {
        return currentUserContext.currentUser()
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication is required."))
            .userId();
    }

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

