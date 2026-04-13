package com.community.mvp.backend.infrastructure.persistence.user.repository.jpa;

import com.community.mvp.backend.infrastructure.persistence.user.entity.CdkEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCdkRepository extends JpaRepository<CdkEntity, Long> {

    Optional<CdkEntity> findByCode(String code);

    Optional<CdkEntity> findByCodeAndUsedFalse(String code);

    List<CdkEntity> findAllByUsed(boolean used);

    long countByUsedFalse();
}
