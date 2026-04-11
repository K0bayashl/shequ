package com.community.mvp.backend.domain.user.repository;

import com.community.mvp.backend.domain.user.model.Cdk;
import java.util.List;
import java.util.Optional;

public interface CdkRepository {

    Optional<Cdk> findByCodeAndUsedFalse(String code);

    List<Cdk> findAll();

    List<Cdk> findAllByUsed(boolean used);

    long countByUsedFalse();

    Cdk saveAndFlush(Cdk cdk);
}
