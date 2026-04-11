CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(120) NOT NULL,
    `description` VARCHAR(1000) NOT NULL,
    `cover_image` VARCHAR(255),
    `status` TINYINT NOT NULL DEFAULT 0,
    `moderation_status` TINYINT NOT NULL DEFAULT 0,
    `moderation_reason` VARCHAR(255),
    `moderated_by` BIGINT,
    `moderated_at` DATETIME,
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_course_status_updated_at` (`status`, `updated_at`),
    KEY `idx_course_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `course_chapter` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `course_id` BIGINT NOT NULL,
    `title` VARCHAR(120) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `sort_order` INT NOT NULL,
    `moderation_status` TINYINT NOT NULL DEFAULT 0,
    `moderation_reason` VARCHAR(255),
    `moderated_by` BIGINT,
    `moderated_at` DATETIME,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_chapter_sort` (`course_id`, `sort_order`),
    KEY `idx_course_chapter_course_sort` (`course_id`, `sort_order`),
    CONSTRAINT `fk_course_chapter_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;