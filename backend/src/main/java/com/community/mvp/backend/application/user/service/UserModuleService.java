package com.community.mvp.backend.application.user.service;

import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.infrastructure.security.JwtTokenService;
import com.community.mvp.backend.application.user.command.ChangePasswordCommand;
import com.community.mvp.backend.application.user.command.LoginUserCommand;
import com.community.mvp.backend.application.user.command.RegisterUserCommand;
import com.community.mvp.backend.application.user.query.GetProfileQuery;
import com.community.mvp.backend.application.user.query.GetViewerProfileQuery;
import com.community.mvp.backend.domain.user.model.Cdk;
import com.community.mvp.backend.domain.user.model.UserAccount;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserProfile;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.domain.user.repository.CdkRepository;
import com.community.mvp.backend.domain.user.repository.UserAccountRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class UserModuleService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserAccountRepository userAccountRepository;
    private final CdkRepository cdkRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserModuleService(
        UserAccountRepository userAccountRepository,
        CdkRepository cdkRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.cdkRepository = cdkRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public RegistrationResult register(RegisterUserCommand command) {
        String normalizedUsername = normalizeRequired(command.username(), "username");
        String normalizedEmail = normalizeEmail(command.email());
        String normalizedPassword = normalizeRequired(command.password(), "password");
        String normalizedConfirmPassword = normalizeRequired(command.confirmPassword(), "confirmPassword");
        String normalizedCdkCode = normalizeRequired(command.cdkCode(), "CDK");

        if (!normalizedPassword.equals(normalizedConfirmPassword)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Passwords do not match.");
        }
        if (normalizedPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Password must be at least 8 characters.");
        }
        if (userAccountRepository.existsByUsername(normalizedUsername)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Username already exists.");
        }
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Email already exists.");
        }

        Cdk cdk = cdkRepository.findByCodeAndUsedFalse(normalizedCdkCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "CDK does not exist or has already been used."));

        UserAccount user = new UserAccount(
            null,
            normalizedUsername,
            normalizedEmail,
            passwordEncoder.encode(normalizedPassword),
            null,
            UserRole.MEMBER,
            UserStatus.ACTIVE,
            null,
            null
        );
        user = userAccountRepository.saveAndFlush(user);

        cdkRepository.saveAndFlush(cdk.markUsed(user.id(), LocalDateTime.now()));
        return new RegistrationResult(user.toProfile());
    }

    @Transactional(readOnly = true)
    public AuthenticationResult login(LoginUserCommand command) {
        String normalizedEmail = normalizeEmail(command.email());
        String normalizedPassword = normalizeRequired(command.password(), "password");
        UserAccount user = userAccountRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(normalizedPassword, user.passwordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid email or password.");
        }
        if (user.status() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is disabled.");
        }
        if (user.status() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is not active.");
        }

        UserPrincipal principal = user.toPrincipal(Instant.now());
        return new AuthenticationResult(jwtTokenService.issueToken(principal), user.toProfile());
    }

    @Transactional
    public UserProfile changePassword(ChangePasswordCommand command) {
        UserAccount user = userAccountRepository.findById(command.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Current user not found."));

        if (user.status() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is disabled.");
        }

        String normalizedOldPassword = normalizeRequired(command.oldPassword(), "oldPassword");
        String normalizedNewPassword = normalizeRequired(command.newPassword(), "newPassword");
        String normalizedConfirmNewPassword = normalizeRequired(command.confirmNewPassword(), "confirmNewPassword");

        if (!passwordEncoder.matches(normalizedOldPassword, user.passwordHash())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Old password is incorrect.");
        }
        if (!normalizedNewPassword.equals(normalizedConfirmNewPassword)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "New passwords do not match.");
        }
        if (normalizedNewPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "New password must be at least 8 characters.");
        }

        user = user.withPasswordHash(passwordEncoder.encode(normalizedNewPassword));
        user = userAccountRepository.saveAndFlush(user);
        return user.toProfile();
    }

    @Transactional(readOnly = true)
    public UserProfile getProfile(GetProfileQuery query) {
        return userAccountRepository.findById(query.userId())
            .map(UserAccount::toProfile)
            .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "User not found."));
    }

    @Transactional(readOnly = true)
    public UserProfile getProfileForViewer(GetViewerProfileQuery query) {
        UserProfile viewer = getProfile(new GetProfileQuery(query.viewerId()));
        if (!viewer.id().equals(query.targetUserId()) && viewer.role() != UserRole.ADMIN.getCode()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "You are not allowed to view this profile.");
        }
        return getProfile(new GetProfileQuery(query.targetUserId()));
    }

    public void logout() {
        // Stateless JWT logout: the client clears the token.
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " must not be blank.");
        }
        return value.trim();
    }

    private String normalizeEmail(String value) {
        return normalizeRequired(value, "email").toLowerCase();
    }
}

