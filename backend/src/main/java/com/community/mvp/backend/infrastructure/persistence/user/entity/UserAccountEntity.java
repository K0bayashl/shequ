package com.community.mvp.backend.infrastructure.persistence.user.entity;

import com.community.mvp.backend.domain.user.model.UserAccount;
import com.community.mvp.backend.domain.user.model.UserRole;
import com.community.mvp.backend.domain.user.model.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "`user`")
public class UserAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 255)
    private String avatar;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int role;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserAccountEntity() {
    }

    public UserAccountEntity(String username, String email, String password, String avatar, int role, int status) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getRole() {
        return role;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public UserAccount toDomain() {
        return new UserAccount(
            id,
            username,
            email,
            password,
            avatar,
            UserRole.fromCode(role),
            UserStatus.fromCode(status),
            createdAt,
            updatedAt
        );
    }

    public void updateFrom(UserAccount userAccount) {
        this.username = userAccount.username();
        this.email = userAccount.email();
        this.password = userAccount.passwordHash();
        this.avatar = userAccount.avatar();
        this.role = userAccount.role().getCode();
        this.status = userAccount.status().getCode();
    }
}

