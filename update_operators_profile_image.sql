-- =====================================================
-- SQL Script to Add Profile Image Column to operators Table
-- Run this in phpMyAdmin SQL tab
-- =====================================================

-- Add profile_image column to store operator profile image path
ALTER TABLE `operators` 
ADD COLUMN `profile_image` VARCHAR(255) NULL DEFAULT NULL AFTER `availability`;

-- Verify the changes
DESCRIBE `operators`;

-- =====================================================
-- Expected Result:
-- The operators table will now have a profile_image column
-- that can store file paths like: 'uploads/profile_images/operator_16_profile.jpg'
-- =====================================================






















