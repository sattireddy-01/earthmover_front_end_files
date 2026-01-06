-- Add category_id column to operators table
ALTER TABLE `operators` 
ADD COLUMN `category_id` INT(11) DEFAULT NULL 
AFTER `equipment_type`;

-- Update existing records based on equipment_type
-- Backhoe Loader → category_id = 1
UPDATE `operators` 
SET `category_id` = 1 
WHERE `equipment_type` = 'Backhoe Loader' OR `equipment_type` = 'backhoe loader';

-- Excavator → category_id = 2
UPDATE `operators` 
SET `category_id` = 2 
WHERE `equipment_type` = 'Excavator' OR `equipment_type` = 'excavator';

-- Dozer → category_id = 3
UPDATE `operators` 
SET `category_id` = 3 
WHERE `equipment_type` = 'Dozer' OR `equipment_type` = 'dozer';

-- Optional: Create a trigger to automatically set category_id when equipment_type is inserted/updated
DELIMITER $$

CREATE TRIGGER `set_category_id_on_insert` 
BEFORE INSERT ON `operators`
FOR EACH ROW
BEGIN
    IF NEW.equipment_type = 'Backhoe Loader' OR NEW.equipment_type = 'backhoe loader' THEN
        SET NEW.category_id = 1;
    ELSEIF NEW.equipment_type = 'Excavator' OR NEW.equipment_type = 'excavator' THEN
        SET NEW.category_id = 2;
    ELSEIF NEW.equipment_type = 'Dozer' OR NEW.equipment_type = 'dozer' THEN
        SET NEW.category_id = 3;
    END IF;
END$$

CREATE TRIGGER `set_category_id_on_update` 
BEFORE UPDATE ON `operators`
FOR EACH ROW
BEGIN
    IF NEW.equipment_type = 'Backhoe Loader' OR NEW.equipment_type = 'backhoe loader' THEN
        SET NEW.category_id = 1;
    ELSEIF NEW.equipment_type = 'Excavator' OR NEW.equipment_type = 'excavator' THEN
        SET NEW.category_id = 2;
    ELSEIF NEW.equipment_type = 'Dozer' OR NEW.equipment_type = 'dozer' THEN
        SET NEW.category_id = 3;
    ELSE
        SET NEW.category_id = NULL;
    END IF;
END$$

DELIMITER ;














