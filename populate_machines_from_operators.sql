-- Update machines table with operator data based on operator_id
-- This will populate address, equipment_type, machine_model, machine_year, 
-- machine_image_1, availability, and profile_image from operators table

UPDATE `machines` m
INNER JOIN `operators` o ON m.operator_id = o.operator_id
SET 
    m.address = o.address,
    m.equipment_type = o.equipment_type,
    m.machine_model = o.machine_model,
    m.machine_year = o.machine_year,
    m.machine_image_1 = o.machine_image_1,
    m.availability = o.availability,
    m.profile_image = o.profile_image
WHERE m.operator_id IS NOT NULL;

-- Optional: Create a trigger to automatically update machines table when operator data changes
DELIMITER $$

CREATE TRIGGER `update_machine_from_operator` 
AFTER UPDATE ON `operators`
FOR EACH ROW
BEGIN
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
END$$

DELIMITER ;














