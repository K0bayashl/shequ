package com.community.mvp.backend.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserContext {

    public Optional<String> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalString && !principalString.isBlank()) {
            return Optional.of(principalString);
        }

        if (authentication.getName() != null && !authentication.getName().isBlank()) {
            return Optional.of(authentication.getName());
        }

        return Optional.empty();
    }
}
