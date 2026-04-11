package com.community.mvp.backend.domain.user.repository;

import com.community.mvp.backend.domain.user.model.UserAccount;
import com.community.mvp.backend.domain.user.model.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository {

    Optional<UserAccount> findById(Long id);

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countAll();

    long countByStatus(UserStatus status);

    List<UserAccount> findAllByIdIn(Collection<Long> userIds);

    UserAccount saveAndFlush(UserAccount userAccount);
}
