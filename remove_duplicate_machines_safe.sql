-- SQL Script to Remove Duplicate Machines with NULL Images
-- This handles foreign key constraints from the bookings table

-- Step 1: Check which machines with NULL images are referenced in bookings
SELECT 
    m.machine_id,
    m.model_name,
    m.image,
    COUNT(b.booking_id) as booking_count
FROM machines m
LEFT JOIN bookings b ON m.machine_id = b.machine_id
WHERE m.image IS NULL
GROUP BY m.machine_id, m.model_name, m.image;

-- Step 2: Find corresponding machines with images (to map bookings to)
SELECT 
    m_old.machine_id as old_machine_id,
    m_old.model_name as old_model_name,
    m_new.machine_id as new_machine_id,
    m_new.model_name as new_model_name,
    m_new.image
FROM machines m_old
INNER JOIN machines m_new ON m_old.model_name = m_new.model_name 
    AND m_old.category_id = m_new.category_id
    AND m_old.price_per_hour = m_new.price_per_hour
WHERE m_old.image IS NULL 
    AND m_new.image IS NOT NULL
ORDER BY m_old.machine_id;

-- Step 3: Update bookings to reference machines with images instead
-- This updates bookings that reference machines with NULL images to point to machines with images
UPDATE bookings b
INNER JOIN machines m_old ON b.machine_id = m_old.machine_id
INNER JOIN machines m_new ON m_old.model_name = m_new.model_name 
    AND m_old.category_id = m_new.category_id
    AND m_old.price_per_hour = m_new.price_per_hour
SET b.machine_id = m_new.machine_id
WHERE m_old.image IS NULL 
    AND m_new.image IS NOT NULL;

-- Step 4: Verify bookings were updated correctly
SELECT 
    b.booking_id,
    b.machine_id,
    m.model_name,
    m.image
FROM bookings b
INNER JOIN machines m ON b.machine_id = m.machine_id
WHERE m.image IS NOT NULL
ORDER BY b.booking_id
LIMIT 10;

-- Step 5: Now delete machines with NULL images (after updating bookings)
DELETE FROM machines
WHERE image IS NULL;

-- Step 6: Verify final result
SELECT 
    machine_id,
    category_id,
    model_name,
    price_per_hour,
    specs,
    model_year,
    image
FROM machines
ORDER BY machine_id;


















