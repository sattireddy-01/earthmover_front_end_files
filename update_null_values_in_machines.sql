-- Update all NULL values in machines table from operators table
-- This will populate any NULL fields in machines with data from linked operators

UPDATE `machines` m
INNER JOIN `operators` o ON m.operator_id = o.operator_id
SET 
    -- Update phone if NULL
    m.phone = COALESCE(m.phone, o.phone),
    -- Update address if NULL
    m.address = COALESCE(m.address, o.address),
    -- Update equipment_type if NULL
    m.equipment_type = COALESCE(m.equipment_type, o.equipment_type),
    -- Update machine_model if NULL
    m.machine_model = COALESCE(m.machine_model, o.machine_model),
    -- Update machine_year if NULL
    m.machine_year = COALESCE(m.machine_year, o.machine_year),
    -- Update machine_image_1 if NULL
    m.machine_image_1 = COALESCE(m.machine_image_1, o.machine_image_1),
    -- Update availability if NULL
    m.availability = COALESCE(m.availability, o.availability),
    -- Update profile_image if NULL
    m.profile_image = COALESCE(m.profile_image, o.profile_image)
WHERE m.operator_id IS NOT NULL;

-- Alternative: Update all fields regardless of NULL (overwrites existing values)
-- Use this if you want to sync all data from operators to machines
/*
UPDATE `machines` m
INNER JOIN `operators` o ON m.operator_id = o.operator_id
SET 
    m.phone = o.phone,
    m.address = o.address,
    m.equipment_type = o.equipment_type,
    m.machine_model = o.machine_model,
    m.machine_year = o.machine_year,
    m.machine_image_1 = o.machine_image_1,
    m.availability = o.availability,
    m.profile_image = o.profile_image
WHERE m.operator_id IS NOT NULL;
*/














