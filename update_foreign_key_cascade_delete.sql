-- Option 1: Change to CASCADE DELETE (machines will be deleted when operator is deleted)
-- First, drop the existing foreign key constraint
ALTER TABLE `machines`
DROP FOREIGN KEY `fk_machines_operator`;

-- Recreate with CASCADE DELETE
ALTER TABLE `machines`
ADD CONSTRAINT `fk_machines_operator` 
FOREIGN KEY (`operator_id`) REFERENCES `operators` (`operator_id`) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Option 2: Keep SET NULL (machines will remain but operator_id will be NULL)
-- This is the current setting - no changes needed if you want this behavior














