package com.community.mvp.backend.application.content.service;

import com.community.mvp.backend.application.content.command.CreateAdminCdkCommand;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import com.community.mvp.backend.domain.user.model.Cdk;
import com.community.mvp.backend.domain.user.repository.CdkRepository;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class ContentSyncAdminService {

    private static final Pattern CDK_CODE_PATTERN = Pattern.compile("^[A-Z0-9-]{6,50}$");

    private final CdkRepository cdkRepository;

    public ContentSyncAdminService(CdkRepository cdkRepository) {
        this.cdkRepository = cdkRepository;
    }

    @Transactional
    public CreateAdminCdkResult createCdk(CreateAdminCdkCommand command) {
        requireOperator(command.operatorUserId());
        String normalizedCode = normalizeCode(command.code());

        if (!CDK_CODE_PATTERN.matcher(normalizedCode).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "CDK code format is invalid.");
        }
        if (cdkRepository.findByCode(normalizedCode).isPresent()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "CDK already exists.");
        }

        Cdk saved = cdkRepository.saveAndFlush(new Cdk(
            null,
            normalizedCode,
            false,
            null,
            null,
            null,
            null
        ));

        return new CreateAdminCdkResult(saved.id(), saved.code(), saved.createdAt());
    }

    private void requireOperator(Long operatorUserId) {
        if (operatorUserId == null || operatorUserId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication is required.");
        }
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "code must not be blank.");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}