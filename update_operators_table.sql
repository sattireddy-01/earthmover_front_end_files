-- =====================================================
-- SQL Script to Update operators Table
-- Add Machine Model and Machine Images Columns
-- Run this in phpMyAdmin SQL tab
-- =====================================================

-- Add machine_model column to store machine model name
ALTER TABLE `operators` 
ADD COLUMN `machine_model` VARCHAR(100) NULL DEFAULT NULL AFTER `rc_number`;

-- Add machine_image_1 column to store first machine image path
ALTER TABLE `operators` 
ADD COLUMN `machine_image_1` VARCHAR(255) NULL DEFAULT NULL AFTER `machine_model`;

-- Add machine_image_2 column to store second machine image path
ALTER TABLE `operators` 
ADD COLUMN `machine_image_2` VARCHAR(255) NULL DEFAULT NULL AFTER `machine_image_1`;

-- Add machine_image_3 column to store third machine image path
ALTER TABLE `operators` 
ADD COLUMN `machine_image_3` VARCHAR(255) NULL DEFAULT NULL AFTER `machine_image_2`;

-- Optional: Add machine_year column if you want to store year separately
ALTER TABLE `operators` 
ADD COLUMN `machine_year` INT(11) NULL DEFAULT NULL AFTER `machine_model`;

-- Optional: Add machine_capacity column if you want to store capacity separately
ALTER TABLE `operators` 
ADD COLUMN `machine_capacity` VARCHAR(50) NULL DEFAULT NULL AFTER `machine_year`;

-- Verify the changes
DESCRIBE `operators`;






















