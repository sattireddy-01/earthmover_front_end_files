-- Add phone column to machines table
ALTER TABLE `machines` 
ADD COLUMN `phone` VARCHAR(15) DEFAULT NULL 
AFTER `operator_id`;

-- Update existing machines with phone numbers from operators table
UPDATE `machines` m
INNER JOIN `operators` o ON m.operator_id = o.operator_id
SET m.phone = o.phone
WHERE m.operator_id IS NOT NULL;

-- Update the trigger to also update phone when operator is updated
DELIMITER $$

DROP TRIGGER IF EXISTS `update_machine_from_operator`$$

CREATE TRIGGER `update_machine_from_operator` 
AFTER UPDATE ON `operators`
FOR EACH ROW
BEGIN
    -- Only update machines if operator has category_id and equipment_type
    IF NEW.category_id IS NOT NULL AND NEW.equipment_type IS NOT NULL THEN
        -- First, try to update existing machine linked to this operator
        UPDATE `machines`
        SET 
            phone = NEW.phone,
            address = NEW.address,
            equipment_type = NEW.equipment_type,
            machine_model = NEW.machine_model,
            machine_year = NEW.machine_year,
            machine_image_1 = NEW.machine_image_1,
            availability = NEW.availability,
            profile_image = NEW.profile_image
        WHERE operator_id = NEW.operator_id;
        
        -- If no machine is linked yet, link one from the same category
        IF ROW_COUNT() = 0 THEN
            UPDATE `machines`
            SET 
                operator_id = NEW.operator_id,
                phone = NEW.phone,
                address = NEW.address,
                equipment_type = NEW.equipment_type,
                machine_model = NEW.machine_model,
                machine_year = NEW.machine_year,
                machine_image_1 = NEW.machine_image_1,
                availability = NEW.availability,
                profile_image = NEW.profile_image
            WHERE category_id = NEW.category_id 
            AND operator_id IS NULL
            LIMIT 1;
        END IF;
    END IF;
END$$

DELIMITER ;














