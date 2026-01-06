-- Manually link operator 46 to a machine (Dozer, category_id = 3)
-- This will update machine_id 7 which has category_id 3 and operator_id IS NULL

UPDATE `machines` m
INNER JOIN `operators` o ON o.operator_id = 46
SET 
    m.operator_id = 46,
    m.phone = o.phone,
    m.address = o.address,
    m.equipment_type = o.equipment_type,
    m.machine_model = o.machine_model,
    m.machine_year = o.machine_year,
    m.machine_image_1 = o.machine_image_1,
    m.availability = o.availability,
    m.profile_image = o.profile_image
WHERE m.category_id = 3 
AND m.operator_id IS NULL
LIMIT 1;














