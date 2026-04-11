package com.community.mvp.backend.domain.moderation.model;

public enum ReportStatus {
    PENDING(0, "pending"),
    RESOLVED(1, "resolved"),
    REJECTED(2, "rejected");

    private final int code;
    private final String keyword;

    ReportStatus(int code, String keyword) {
        this.code = code;
        this.keyword = keyword;
    }

    public int getCode() {
        return code;
    }

    public String getKeyword() {
        return keyword;
    }

    public static ReportStatus fromCode(int code) {
        for (ReportStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown report status code: " + code);
    }

    public static ReportStatus fromKeyword(String keyword) {
        for (ReportStatus status : values()) {
            if (status.keyword.equalsIgnoreCase(keyword)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown report status keyword: " + keyword);
    }
}
