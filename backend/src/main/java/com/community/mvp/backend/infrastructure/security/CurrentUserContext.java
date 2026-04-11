package com.community.mvp.backend.infrastructure.security;

import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.domain.user.model.UserStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserContext {

    public Optional<UserPrincipal> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal);
        }

        if (principal instanceof String principalString && !principalString.isBlank()) {
            return Optional.of(new UserPrincipal(
                parseLong(principalString),
                principalString,
                "",
                UserRole.MEMBER.getCode(),
                UserStatus.ACTIVE.getCode(),
                null
            ));
        }

        return Optional.empty();
    }

    public Optional<String> currentUserId() {
        return currentUser().map(user -> String.valueOf(user.userId()));
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }
}

