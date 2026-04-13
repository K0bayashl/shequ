package com.community.mvp.backend.application.content.command;

public record CreateAdminCdkCommand(
    String code,
    Long operatorUserId
) {
}