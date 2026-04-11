CREATE TABLE IF NOT EXISTS `content_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `content_type` VARCHAR(32) NOT NULL,
    `content_id` BIGINT NOT NULL,
    `reporter_user_id` BIGINT NOT NULL,
    `reason_code` VARCHAR(32) NOT NULL,
    `reason_detail` VARCHAR(500),
    `status` TINYINT NOT NULL DEFAULT 0,
    `handled_by` BIGINT,
    `handled_at` DATETIME,
    `handle_note` VARCHAR(500),
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_content_report_status_created_at` (`status`, `created_at`),
    KEY `idx_content_report_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `action_audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `actor_user_id` BIGINT NOT NULL,
    `action_type` VARCHAR(64) NOT NULL,
    `target_type` VARCHAR(32) NOT NULL,
    `target_id` BIGINT NOT NULL,
    `action_result` VARCHAR(32) NOT NULL,
    `detail_json` TEXT,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_action_audit_created_at` (`created_at`),
    KEY `idx_action_audit_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
