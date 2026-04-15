-- Tables will be auto-created by Hibernate with spring.jpa.hibernate.ddl-auto=update
-- This file is for reference only

-- Reactions table
CREATE TABLE IF NOT EXISTS reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY unique_user_post_reaction (post_id, user_id),
    INDEX idx_post_type (post_id, type),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Comments table
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    user_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_post_parent (post_id, parent_comment_id),
    INDEX idx_parent_comment (parent_comment_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE
);
