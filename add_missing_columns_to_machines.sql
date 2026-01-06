-- Add only the missing columns to machines table (skip operator_id if it already exists)
-- Check and add address column
ALTER TABLE `machines` 
ADD COLUMN `address` TEXT DEFAULT NULL AFTER `image`;

-- Add equipment_type column
ALTER TABLE `machines` 
ADD COLUMN `equipment_type` VARCHAR(50) DEFAULT NULL AFTER `address`;

-- Add machine_model column
ALTER TABLE `machines` 
ADD COLUMN `machine_model` VARCHAR(100) DEFAULT NULL AFTER `equipment_type`;

-- Add machine_year column
ALTER TABLE `machines` 
ADD COLUMN `machine_year` INT(11) DEFAULT NULL AFTER `machine_model`;

-- Add machine_image_1 column
ALTER TABLE `machines` 
ADD COLUMN `machine_image_1` VARCHAR(255) DEFAULT NULL AFTER `machine_year`;

-- Add availability column
ALTER TABLE `machines` 
ADD COLUMN `availability` ENUM('ONLINE','OFFLINE') DEFAULT 'OFFLINE' AFTER `machine_image_1`;

-- Add profile_image column
ALTER TABLE `machines` 
ADD COLUMN `profile_image` VARCHAR(255) DEFAULT NULL AFTER `availability`;

-- Add foreign key constraint (only if operator_id exists and constraint doesn't exist)
-- First check if constraint exists, if not add it
ALTER TABLE `machines`
ADD CONSTRAINT `fk_machines_operator` 
FOREIGN KEY (`operator_id`) REFERENCES `operators` (`operator_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- Add index on operator_id (if it doesn't exist)
ALTER TABLE `machines`
ADD INDEX `idx_operator_id` (`operator_id`);














