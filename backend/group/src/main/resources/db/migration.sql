-- Migration script for changing userId types from BIGINT to VARCHAR
-- Execute this if you have existing data in the groups and group_members tables

-- Backup your data first!
-- CREATE TABLE groups_backup AS SELECT * FROM groups;
-- CREATE TABLE group_members_backup AS SELECT * FROM group_members;

-- Modify creator_id in groups table
ALTER TABLE groups 
MODIFY COLUMN creator_id VARCHAR(255) NOT NULL;

-- Modify user_id in group_members table
ALTER TABLE group_members 
MODIFY COLUMN user_id VARCHAR(255) NOT NULL;

-- Modify invited_by in group_members table
ALTER TABLE group_members 
MODIFY COLUMN invited_by VARCHAR(255);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_groups_creator_id ON groups(creator_id);
CREATE INDEX IF NOT EXISTS idx_group_members_user_id ON group_members(user_id);
CREATE INDEX IF NOT EXISTS idx_group_members_group_user ON group_members(group_id, user_id);
