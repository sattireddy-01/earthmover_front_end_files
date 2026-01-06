# Update Operators Table - SQL Script

## Purpose
Add columns to the `operators` table to store machine model and machine images.

## How to Run

1. Open phpMyAdmin
2. Select the `earthmover` database
3. Click on the **SQL** tab
4. Copy and paste the SQL code from `update_operators_table.sql`
5. Click **Go** to execute

## Columns Added

| Column Name | Type | Description |
|-------------|------|-------------|
| `machine_model` | VARCHAR(100) | Machine model name (e.g., "JCB 3DX", "Tata Hitachi EX 110") |
| `machine_year` | INT(11) | Year of the machine (optional) |
| `machine_capacity` | VARCHAR(50) | Machine capacity (optional) |
| `machine_image_1` | VARCHAR(255) | Path to first machine image |
| `machine_image_2` | VARCHAR(255) | Path to second machine image |
| `machine_image_3` | VARCHAR(255) | Path to third machine image |

## Updated Table Structure

After running the SQL, your `operators` table will have:

```sql
CREATE TABLE `operators` (
  `operator_id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `license_no` varchar(50) DEFAULT NULL,
  `rc_number` varchar(50) DEFAULT NULL,
  `machine_model` varchar(100) DEFAULT NULL,        -- NEW
  `machine_year` int(11) DEFAULT NULL,              -- NEW
  `machine_capacity` varchar(50) DEFAULT NULL,      -- NEW
  `machine_image_1` varchar(255) DEFAULT NULL,      -- NEW
  `machine_image_2` varchar(255) DEFAULT NULL,      -- NEW
  `machine_image_3` varchar(255) DEFAULT NULL,     -- NEW
  `approve_status` enum('APPROVED','REJECTED','PENDING') DEFAULT 'PENDING',
  `approval_pending` tinyint(1) DEFAULT 1,
  `availability` enum('ONLINE','OFFLINE') DEFAULT 'OFFLINE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

## Image Storage

The image columns will store file paths like:
- `uploads/machine_images/machine_10_1735467890.jpg`
- `uploads/machine_images/machine_10_1735467891.jpg`
- `uploads/machine_images/machine_10_1735467892.jpg`

## Notes

- All new columns are **NULL** by default, so existing records won't be affected
- Images will be stored in `C:\xampp\htdocs\Earth_mover\uploads\machine_images\`
- Make sure the `uploads/machine_images/` directory exists and has write permissions






















