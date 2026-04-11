package com.community.mvp.backend.domain.user.service;

import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.domain.user.model.UserAccount;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.domain.user.repository.UserAccountRepository;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class UserAuthenticationVerifierImpl implements UserAuthenticationVerifier {

    private final UserAccountRepository userAccountRepository;

    public UserAuthenticationVerifierImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserPrincipal verify(Long userId, Instant issuedAt) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Token is no longer valid. Please sign in again."));

        if (user.status() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is disabled.");
        }
        if (user.status() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is not active.");
        }
        if (issuedAt.isBefore(user.updatedAt().atZone(ZoneId.systemDefault()).toInstant())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Password has changed. Please sign in again.");
        }
        return user.toPrincipal(issuedAt);
    }
}

