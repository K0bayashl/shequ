package com.community.mvp.backend.domain.content.model;

public enum CourseStatus {
    DRAFT(0),
    PUBLISHED(1),
    OFFLINE(2);

    private final int code;

    CourseStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public static CourseStatus fromCode(int code) {
        for (CourseStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown course status code: " + code);
    }
}