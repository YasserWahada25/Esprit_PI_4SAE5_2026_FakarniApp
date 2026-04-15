-- Ajouter les nouvelles colonnes à la table groups
ALTER TABLE groups ADD COLUMN creator_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE groups ADD COLUMN group_type VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE groups ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE groups ADD COLUMN cover_image_url VARCHAR(500);
ALTER TABLE groups ADD COLUMN max_members INT;
ALTER TABLE groups ADD COLUMN is_joinable BOOLEAN NOT NULL DEFAULT TRUE;

-- Créer la table group_members
CREATE TABLE group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invited_by BIGINT,
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT uk_group_user UNIQUE (group_id, user_id)
);

-- Créer des index pour améliorer les performances
CREATE INDEX idx_group_members_group_id ON group_members(group_id);
CREATE INDEX idx_group_members_user_id ON group_members(user_id);
CREATE INDEX idx_group_members_role ON group_members(role);
CREATE INDEX idx_groups_creator_id ON groups(creator_id);
CREATE INDEX idx_groups_status ON groups(status);
CREATE INDEX idx_groups_group_type ON groups(group_type);
