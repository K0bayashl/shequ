package com.community.mvp.backend.infrastructure.persistence.user.entity;

import com.community.mvp.backend.domain.user.model.Cdk;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.NumericBooleanConverter;

@Entity
@Table(name = "cdk")
public class CdkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "is_used", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean used;

    @Column(name = "used_by")
    private Long usedBy;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CdkEntity() {
    }

    public CdkEntity(String code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public boolean isUsed() {
        return used;
    }

    public Long getUsedBy() {
        return usedBy;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void markUsed(Long userId, LocalDateTime usedAt) {
        this.used = true;
        this.usedBy = userId;
        this.usedAt = usedAt;
    }

    public Cdk toDomain() {
        return new Cdk(id, code, used, usedBy, usedAt, createdAt, updatedAt);
    }

    public void updateFrom(Cdk cdk) {
        this.code = cdk.code();
        this.used = cdk.used();
        this.usedBy = cdk.usedBy();
        this.usedAt = cdk.usedAt();
    }
}

