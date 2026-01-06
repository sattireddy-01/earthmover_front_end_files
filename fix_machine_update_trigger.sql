-- Drop the existing INSERT trigger since operators don't have category_id during signup
DROP TRIGGER IF EXISTS `populate_machine_from_new_operator`;

-- Fix the UPDATE trigger to properly link machines when operator license details are saved
DELIMITER $$

DROP TRIGGER IF EXISTS `update_machine_from_operator`$$

CREATE TRIGGER `update_machine_from_operator` 
AFTER UPDATE ON `operators`
FOR EACH ROW
BEGIN
    -- Only update machines if operator has category_id and equipment_type (license details submitted)
    IF NEW.category_id IS NOT NULL AND NEW.equipment_type IS NOT NULL THEN
        -- First, try to update existing machine linked to this operator
        UPDATE `machines`
        SET 
            address = NEW.address,
            equipment_type = NEW.equipment_type,
            machine_model = NEW.machine_model,
            machine_year = NEW.machine_year,
            machine_image_1 = NEW.machine_image_1,
            availability = NEW.availability,
            profile_image = NEW.profile_image
        WHERE operator_id = NEW.operator_id;
        
        -- If no machine is linked yet (ROW_COUNT() = 0), link one from the same category
        IF ROW_COUNT() = 0 THEN
            UPDATE `machines`
            SET 
                operator_id = NEW.operator_id,
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
