package com.community.mvp.backend.domain.user.model;

public enum UserStatus {
    INACTIVE(0),
    ACTIVE(1),
    DISABLED(2);

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserStatus fromCode(int code) {
        for (UserStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status code: " + code);
    }
}

