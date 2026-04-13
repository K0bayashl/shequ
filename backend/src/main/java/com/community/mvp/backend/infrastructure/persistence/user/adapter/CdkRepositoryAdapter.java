package com.community.mvp.backend.infrastructure.persistence.user.adapter;

import com.community.mvp.backend.domain.user.model.Cdk;
import com.community.mvp.backend.infrastructure.persistence.user.entity.CdkEntity;
import com.community.mvp.backend.infrastructure.persistence.user.repository.jpa.JpaCdkRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class CdkRepositoryAdapter implements com.community.mvp.backend.domain.user.repository.CdkRepository {

    private final JpaCdkRepository cdkRepository;

    public CdkRepositoryAdapter(JpaCdkRepository cdkRepository) {
        this.cdkRepository = cdkRepository;
    }

    @Override
    public Optional<Cdk> findByCode(String code) {
        return cdkRepository.findByCode(code).map(CdkEntity::toDomain);
    }

    @Override
    public Optional<Cdk> findByCodeAndUsedFalse(String code) {
        return cdkRepository.findByCodeAndUsedFalse(code).map(CdkEntity::toDomain);
    }

    @Override
    public List<Cdk> findAll() {
        return cdkRepository.findAll().stream().map(CdkEntity::toDomain).toList();
    }

    @Override
    public List<Cdk> findAllByUsed(boolean used) {
        return cdkRepository.findAllByUsed(used).stream().map(CdkEntity::toDomain).toList();
    }

    @Override
    public long countByUsedFalse() {
        return cdkRepository.countByUsedFalse();
    }

    @Override
    public Cdk saveAndFlush(Cdk cdk) {
        CdkEntity entity = cdk.id() == null
            ? new CdkEntity()
            : cdkRepository.findById(cdk.id())
                .orElseThrow(() -> new IllegalStateException("CDK does not exist."));
        entity.updateFrom(cdk);
        return cdkRepository.saveAndFlush(entity).toDomain();
    }
}
