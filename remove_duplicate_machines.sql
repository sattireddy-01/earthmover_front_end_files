-- SQL Script to Remove Duplicate Machines with NULL Images
-- This will delete machines that have NULL in the image column
-- Keep the machines that have images (uploaded via Postman)

-- First, let's see what will be deleted (for safety)
SELECT 
    machine_id,
    category_id,
    model_name,
    price_per_hour,
    specs,
    model_year,
    image
FROM machines
WHERE image IS NULL
ORDER BY machine_id;

-- Delete machines with NULL images
-- WARNING: This will permanently delete these records
DELETE FROM machines
WHERE image IS NULL;

-- Verify the deletion
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


















