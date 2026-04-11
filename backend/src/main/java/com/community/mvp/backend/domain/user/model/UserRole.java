package com.community.mvp.backend.domain.user.model;

public enum UserRole {
    MEMBER(0),
    ADMIN(1);

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserRole fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role code: " + code);
    }
}

