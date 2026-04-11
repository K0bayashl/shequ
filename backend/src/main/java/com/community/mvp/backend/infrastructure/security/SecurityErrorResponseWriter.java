package com.community.mvp.backend.infrastructure.security;

import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.common.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        write(response, 401, ErrorCode.UNAUTHORIZED, message);
    }

    public void writeForbidden(HttpServletResponse response, String message) throws IOException {
        write(response, 403, ErrorCode.FORBIDDEN, message);
    }

    private void write(HttpServletResponse response, int status, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(errorCode, message, null));
    }
}

