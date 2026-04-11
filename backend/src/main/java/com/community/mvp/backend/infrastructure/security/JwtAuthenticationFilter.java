package com.community.mvp.backend.infrastructure.security;

import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.domain.user.service.UserAuthenticationVerifier;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.community.mvp.backend.domain.user.model.UserRole;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final ObjectProvider<UserAuthenticationVerifier> userAuthenticationVerifierProvider;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    public JwtAuthenticationFilter(
        JwtTokenService jwtTokenService,
        ObjectProvider<UserAuthenticationVerifier> userAuthenticationVerifierProvider,
        SecurityErrorResponseWriter securityErrorResponseWriter
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userAuthenticationVerifierProvider = userAuthenticationVerifierProvider;
        this.securityErrorResponseWriter = securityErrorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        Optional<UserPrincipal> principal = jwtTokenService.parseBearerToken(authorization);
        if (principal.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UserAuthenticationVerifier verifier = userAuthenticationVerifierProvider.getIfAvailable();
            UserPrincipal authenticatedUser = verifier == null || isPublicEndpoint(request)
                ? principal.get()
                : verifier.verify(principal.get().userId(), principal.get().issuedAt());

            SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                    authenticatedUser,
                    principal.get(),
                    List.of(new SimpleGrantedAuthority(roleToAuthority(authenticatedUser.role())))
                )
            );
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.FORBIDDEN) {
                securityErrorResponseWriter.writeForbidden(response, exception.getMessage());
            } else {
                securityErrorResponseWriter.writeUnauthorized(response, exception.getMessage());
            }
        }
    }

    private String roleToAuthority(Integer role) {
        if (role != null && role == UserRole.ADMIN.getCode()) {
            return "ROLE_ADMIN";
        }
        return "ROLE_MEMBER";
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/actuator/health")
            || path.startsWith("/actuator/health/")
            || path.equals("/actuator/info")
            || path.equals("/api/v1/scaffold/ping")
            || path.equals("/api/v1/scaffold/echo")
            || path.equals("/api/users/register")
            || path.equals("/api/users/login");
    }
}

