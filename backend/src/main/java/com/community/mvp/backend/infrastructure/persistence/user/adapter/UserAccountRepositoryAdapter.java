package com.community.mvp.backend.infrastructure.persistence.user.adapter;

import com.community.mvp.backend.domain.user.model.UserAccount;
import com.community.mvp.backend.domain.user.model.UserStatus;
import com.community.mvp.backend.infrastructure.persistence.user.entity.UserAccountEntity;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaUserAccountRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class UserAccountRepositoryAdapter implements com.community.mvp.backend.domain.user.repository.UserAccountRepository {

    private final JpaUserAccountRepository userAccountRepository;

    public UserAccountRepositoryAdapter(JpaUserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        return userAccountRepository.findById(id).map(UserAccountEntity::toDomain);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return userAccountRepository.findByUsername(username).map(UserAccountEntity::toDomain);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return userAccountRepository.findByEmail(email).map(UserAccountEntity::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userAccountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userAccountRepository.existsByEmail(email);
    }

    @Override
    public long countAll() {
        return userAccountRepository.count();
    }

    @Override
    public long countByStatus(UserStatus status) {
        return userAccountRepository.countByStatus(status.getCode());
    }

    @Override
    public List<UserAccount> findAllByIdIn(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return userAccountRepository.findAllByIdIn(userIds).stream().map(UserAccountEntity::toDomain).toList();
    }

    @Override
    public UserAccount saveAndFlush(UserAccount userAccount) {
        UserAccountEntity entity = userAccount.id() == null
            ? new UserAccountEntity()
            : userAccountRepository.findById(userAccount.id())
                .orElseThrow(() -> new IllegalStateException("User account does not exist."));
        entity.updateFrom(userAccount);
        return userAccountRepository.saveAndFlush(entity).toDomain();
    }
}
