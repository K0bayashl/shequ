package com.community.mvp.backend.common.api;

import com.community.mvp.backend.common.error.ErrorCode;
import java.time.Instant;

public record ApiResponse<T>(
    String code,
    String message,
    T data,
    Instant timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data, Instant.now());
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message, T data) {
        return new ApiResponse<>(errorCode.getCode(), message, data, Instant.now());
    }
}
