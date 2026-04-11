package com.community.mvp.backend.domain.moderation.model;

public enum ReportContentType {
    COURSE("course");

    private final String code;

    ReportContentType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReportContentType fromCode(String code) {
        for (ReportContentType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown report content type: " + code);
    }
}
