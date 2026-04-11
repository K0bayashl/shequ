package com.community.mvp.backend.application.content.service;

import com.community.mvp.backend.domain.user.model.Cdk;
import com.community.mvp.backend.domain.user.model.UserAccount;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.domain.user.repository.CdkRepository;
import com.community.mvp.backend.domain.user.repository.UserAccountRepository;
import com.community.mvp.backend.interfaces.rest.content.dto.AdminCdkItemResponse;
import com.community.mvp.backend.interfaces.rest.content.dto.AdminCdkOverviewResponse;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class ContentSyncQueryService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CdkRepository cdkRepository;
    private final UserAccountRepository userAccountRepository;

    public ContentSyncQueryService(CdkRepository cdkRepository, UserAccountRepository userAccountRepository) {
        this.cdkRepository = cdkRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public AdminCdkOverviewResponse queryAdminCdks(String search, String status) {
        String normalizedStatus = normalize(status);
        String searchKeyword = normalize(search);

        List<Cdk> candidateCdks = selectCdksByStatus(normalizedStatus);
        Set<Long> usedUserIds = candidateCdks.stream()
            .map(Cdk::usedBy)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toSet());

        Map<Long, UserAccount> userById = userAccountRepository.findAllByIdIn(usedUserIds).stream()
            .collect(Collectors.toMap(UserAccount::id, user -> user));

        List<AdminCdkItemResponse> filteredCdks = candidateCdks.stream()
            .map(cdk -> toResponse(cdk, userById.get(cdk.usedBy())))
            .filter(item -> matchesKeyword(item, searchKeyword))
            .sorted(Comparator.comparing(AdminCdkItemResponse::id).reversed())
            .toList();

        return new AdminCdkOverviewResponse(
            Math.toIntExact(userAccountRepository.countAll()),
            Math.toIntExact(userAccountRepository.countByStatus(UserStatus.ACTIVE)),
            Math.toIntExact(cdkRepository.countByUsedFalse()),
            filteredCdks
        );
    }

    private List<Cdk> selectCdksByStatus(String normalizedStatus) {
        return switch (normalizedStatus) {
            case "used" -> cdkRepository.findAllByUsed(true);
            case "unused" -> cdkRepository.findAllByUsed(false);
            case "all", "" -> cdkRepository.findAll();
            default -> List.of();
        };
    }

    private AdminCdkItemResponse toResponse(Cdk cdk, UserAccount userAccount) {
        String status = cdk.used() ? "used" : "unused";

        return new AdminCdkItemResponse(
            cdk.id(),
            cdk.code(),
            status,
            userAccount == null ? null : userAccount.username(),
            userAccount == null ? null : userAccount.email(),
            cdk.usedAt() == null ? null : DATE_FORMATTER.format(cdk.usedAt()),
            cdk.createdAt() == null ? null : DATE_FORMATTER.format(cdk.createdAt())
        );
    }

    private boolean matchesKeyword(AdminCdkItemResponse item, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        return contains(item.key(), keyword)
            || contains(item.usedBy(), keyword)
            || contains(item.usedByEmail(), keyword);
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
