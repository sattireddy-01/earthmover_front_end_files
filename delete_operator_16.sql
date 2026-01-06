-- Delete operator with operator_id = 16 from operators table
DELETE FROM `operators` 
WHERE `operator_id` = 16;

-- Note: 
-- If foreign key has ON DELETE CASCADE: Related machines will also be deleted
-- If foreign key has ON DELETE SET NULL: Machines will remain but operator_id will be set to NULL














