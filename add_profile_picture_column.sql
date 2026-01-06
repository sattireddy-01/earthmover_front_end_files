-- Add profile_picture column to users table
-- This column will store the file path or URL of the user's profile picture

ALTER TABLE `users` 
ADD COLUMN `profile_picture` VARCHAR(500) DEFAULT NULL 
AFTER `address`;

-- Optional: Add a comment to describe the column
ALTER TABLE `users` 
MODIFY COLUMN `profile_picture` VARCHAR(500) DEFAULT NULL 
COMMENT 'Stores the file path or URL of the user profile picture';





















