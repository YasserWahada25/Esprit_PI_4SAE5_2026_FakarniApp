-- Migration script for adding userId to posts table
-- Execute this if you have existing data in the posts table

-- Add userId column if it doesn't exist
ALTER TABLE posts 
ADD COLUMN IF NOT EXISTS user_id VARCHAR(255) NOT NULL DEFAULT 'unknown';

-- Optional: Update existing posts with a default userId
-- Replace 'default-user-id' with an actual user ID from your User-Service
-- UPDATE posts SET user_id = 'default-user-id' WHERE user_id = 'unknown';

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
