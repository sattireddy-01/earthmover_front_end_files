-- Add operator_id column to machines table to link operators to machines
ALTER TABLE `machines` 
ADD COLUMN `operator_id` INT(11) DEFAULT NULL 
AFTER `machine_id`;

-- Add foreign key constraint to link machines to operators
ALTER TABLE `machines`
ADD CONSTRAINT `fk_machines_operator` 
FOREIGN KEY (`operator_id`) REFERENCES `operators` (`operator_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- Add index on operator_id for better query performance
ALTER TABLE `machines`
ADD INDEX `idx_operator_id` (`operator_id`);

-- Optional: Create a view to display machines with operator details
CREATE OR REPLACE VIEW `machines_with_operators` AS
SELECT 
    m.machine_id,
    m.operator_id,
    m.category_id,
    m.model_name,
    m.price_per_hour,
    m.specs,
    m.model_year,
    m.image,
    o.address,
    o.equipment_type,
    o.machine_model,
    o.machine_year,
    o.machine_image_1,
    o.availability,
    o.profile_image
FROM `machines` m
LEFT JOIN `operators` o ON m.operator_id = o.operator_id;














