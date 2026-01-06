-- Add equipment_type column to operators table
ALTER TABLE `operators` 
ADD COLUMN `equipment_type` VARCHAR(50) DEFAULT NULL 
AFTER `rc_number`;

-- Optional: Add a comment to describe the column
ALTER TABLE `operators` 
MODIFY COLUMN `equipment_type` VARCHAR(50) DEFAULT NULL 
COMMENT 'Equipment type: Backhoe Loader, Excavator, or Dozer';














