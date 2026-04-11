package com.community.mvp.backend.infrastructure.persistence.user.repository.jpa;

import com.community.mvp.backend.infrastructure.persistence.user.entity.UserAccountEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserAccountRepository extends JpaRepository<UserAccountEntity, Long> {

    Optional<UserAccountEntity> findByUsername(String username);

    Optional<UserAccountEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByStatus(int status);

    List<UserAccountEntity> findAllByIdIn(Collection<Long> userIds);
}
