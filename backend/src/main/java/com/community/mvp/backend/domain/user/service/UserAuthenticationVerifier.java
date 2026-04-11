package com.community.mvp.backend.domain.user.service;

import com.community.mvp.backend.domain.user.model.UserPrincipal;
import java.time.Instant;

public interface UserAuthenticationVerifier {

    UserPrincipal verify(Long userId, Instant issuedAt);
}

