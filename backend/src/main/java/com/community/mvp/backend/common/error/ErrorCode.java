package com.community.mvp.backend.common.error;

public enum ErrorCode {
    SUCCESS("SUCCESS", "Request handled successfully."),
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed."),
    REQUEST_BODY_ERROR("REQUEST_BODY_ERROR", "Request body is malformed."),
    BUSINESS_ERROR("BUSINESS_ERROR", "Business rule validation failed."),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication is required."),
    SYSTEM_ERROR("SYSTEM_ERROR", "An unexpected system error occurred.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
