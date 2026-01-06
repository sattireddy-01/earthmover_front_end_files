# Postman Guide: Upload Machine with Image

## 📋 Overview

This guide shows you how to upload machine data **with images** to the backend using Postman.

**Endpoint:** `http://localhost/Earth_mover/api/admin/create_machine.php`  
**Method:** `POST`  
**Content-Type:** `multipart/form-data` (for file uploads)

---

## 🖼️ Method 1: Upload Machine with Image (Form-Data)

### Step-by-Step Instructions:

#### 1. **Open Postman and Create Request**
   - Click "New" → "HTTP Request"
   - Method: Select **POST**
   - URL: `http://localhost/Earth_mover/api/admin/create_machine.php`

#### 2. **Configure Body (Form-Data)**
   - Click **Body** tab
   - Select **form-data** radio button (NOT raw, NOT x-www-form-urlencoded)
   - You'll see a table with Key-Value pairs

#### 3. **Add Text Fields**
   Add these as **Text** fields (not File):

   | Key | Type | Value | Description |
   |-----|------|-------|--------------|
   | `category_id` | Text | `1` | Category ID (1=Backhoe, 2=Excavator, 3=Dozer) |
   | `model_name` | Text | `JCB 3DX` | Machine model name |
   | `price_per_hour` | Text | `1250.00` | Price per hour |
   | `specs` | Text | `Backhoe Loader` | Machine specifications (optional) |
   | `model_year` | Text | `2024` | Year of manufacture (optional) |

   **How to add:**
   - Click "Key" field → Type `category_id`
   - Click dropdown next to "Key" → Select **Text** (not File)
   - Click "Value" field → Type `1`
   - Click outside to save
   - Repeat for each field

#### 4. **Add Image File**
   - Click "Key" field → Type `image`
   - Click dropdown next to "Key" → Select **File** (important!)
   - Click "Select Files" button
   - Choose your image file (JPG, PNG, GIF, or WebP)
   - Max file size: 5MB

#### 5. **Send Request**
   - Click the blue **Send** button
   - Wait for response

---

## 📸 Visual Guide

### Body Tab Configuration:
```
┌─────────────────────────────────────────────┐
│ Body                                        │
├─────────────────────────────────────────────┤
│ ○ none                                      │
│ ● form-data  ← Select this!                 │
│ ○ x-www-form-urlencoded                     │
│ ○ raw                                       │
│ ○ binary                                    │
│ ○ GraphQL                                   │
├─────────────────────────────────────────────┤
│ Key          Type    Value                  │
├─────────────────────────────────────────────┤
│ category_id  [Text]  1                      │
│ model_name   [Text]  JCB 3DX               │
│ price_per... [Text]  1250.00                │
│ specs        [Text]  Backhoe Loader        │
│ model_year   [Text]  2024                   │
│ image        [File]  [Select Files] ← File!│
└─────────────────────────────────────────────┘
```

---

## ✅ Complete Example

### Request Setup:
- **Method:** POST
- **URL:** `http://localhost/Earth_mover/api/admin/create_machine.php`
- **Body Type:** form-data

### Form Fields:

| Key | Type | Value |
|-----|------|-------|
| `category_id` | Text | `1` |
| `model_name` | Text | `JCB 3DX` |
| `price_per_hour` | Text | `1250.00` |
| `specs` | Text | `Backhoe Loader with advanced hydraulic system` |
| `model_year` | Text | `2024` |
| `image` | **File** | `[Select your image file]` |

### Expected Success Response:
```json
{
  "success": true,
  "ok": true,
  "message": "Machine created successfully",
  "machine_id": 6,
  "data": {
    "machine_id": 6,
    "category_id": 1,
    "model_name": "JCB 3DX",
    "price_per_hour": 1250.00,
    "specs": "Backhoe Loader with advanced hydraulic system",
    "model_year": 2024,
    "image": "uploads/machines/machine_1234567890_abc123.jpg"
  }
}
```

---

## 📝 More Examples

### Example 1: Excavator with Image
| Key | Type | Value |
|-----|------|-------|
| `category_id` | Text | `2` |
| `model_name` | Text | `Tata Hitachi EX 110` |
| `price_per_hour` | Text | `1400.00` |
| `specs` | Text | `Heavy Excavator` |
| `model_year` | Text | `2024` |
| `image` | File | `[Select excavator image]` |

### Example 2: Dozer with Image
| Key | Type | Value |
|-----|------|-------|
| `category_id` | Text | `3` |
| `model_name` | Text | `John Deere 5050D` |
| `price_per_hour` | Text | `1200.00` |
| `specs` | Text | `Heavy duty dozer` |
| `model_year` | Text | `2024` |
| `image` | File | `[Select dozer image]` |

### Example 3: Minimal (No Image)
| Key | Type | Value |
|-----|------|-------|
| `category_id` | Text | `1` |
| `model_name` | Text | `JCB 3DX` |
| `price_per_hour` | Text | `1250.00` |

---

## ⚠️ Important Notes

### File Upload Requirements:
- ✅ **Supported formats:** JPG, JPEG, PNG, GIF, WebP
- ✅ **Max file size:** 5MB
- ✅ **Field name:** Must be exactly `image` (lowercase)
- ✅ **Type:** Must select **File** (not Text) in Postman

### Common Mistakes:
- ❌ **Wrong field type:** Selecting "Text" instead of "File" for image
- ❌ **Wrong field name:** Using `Image` or `IMAGE` instead of `image`
- ❌ **File too large:** Exceeding 5MB limit
- ❌ **Wrong format:** Using PDF, DOC, or other non-image formats
- ❌ **Missing required fields:** Forgetting `category_id`, `model_name`, or `price_per_hour`

---

## 🔍 Troubleshooting

### Error: "Failed to upload image"
- ✅ Check file size is under 5MB
- ✅ Check file format is JPG, PNG, GIF, or WebP
- ✅ Make sure you selected **File** type (not Text) in Postman
- ✅ Check file is not corrupted

### Error: "category_id is required"
- ✅ Make sure `category_id` field exists
- ✅ Value must be a number (1, 2, 3, etc.)
- ✅ Value must be greater than 0

### Error: "model_name is required"
- ✅ Make sure `model_name` field exists
- ✅ Value cannot be empty

### Error: "price_per_hour is required"
- ✅ Make sure `price_per_hour` field exists
- ✅ Value must be a number (can have decimals like 1250.00)

### Image not saving:
- ✅ Check `C:\xampp\htdocs\Earth_mover\uploads\machines\` directory exists
- ✅ Check directory has write permissions
- ✅ Check PHP error log: `C:\xampp\php\logs\php_error_log`

---

## 📁 File Storage Location

Images are saved to:
```
C:\xampp\htdocs\Earth_mover\uploads\machines\
```

Example saved file:
```
uploads/machines/machine_1735567890_abc123def456.jpg
```

The database stores the relative path:
```
uploads/machines/machine_1735567890_abc123def456.jpg
```

---

## 🎯 Quick Test

1. **Prepare:**
   - Have an image file ready (JPG or PNG, under 5MB)

2. **In Postman:**
   - Method: POST
   - URL: `http://localhost/Earth_mover/api/admin/create_machine.php`
   - Body: form-data
   - Add fields:
     - `category_id` (Text): `1`
     - `model_name` (Text): `Test Machine`
     - `price_per_hour` (Text): `1000.00`
     - `image` (File): Select your image
   - Click Send

3. **Expected:**
   - Success response with `machine_id`
   - Image path in response
   - File saved in `uploads/machines/` folder

---

## 🔄 Alternative: JSON Method (Without Image)

If you don't need to upload an image, you can use JSON:

**Body Type:** raw → JSON

```json
{
  "category_id": 1,
  "model_name": "JCB 3DX",
  "price_per_hour": 1250.00,
  "specs": "Backhoe Loader",
  "model_year": 2024,
  "image": null
}
```

**Note:** For image uploads, you **must** use form-data. JSON method only accepts image paths (if image is already uploaded separately).

---

## ✅ Checklist

Before sending request:
- [ ] Method is POST
- [ ] URL is correct
- [ ] Body type is **form-data**
- [ ] `category_id` field added (Text type)
- [ ] `model_name` field added (Text type)
- [ ] `price_per_hour` field added (Text type)
- [ ] `image` field added (**File** type, not Text!)
- [ ] Image file selected
- [ ] Image file is under 5MB
- [ ] Image format is JPG, PNG, GIF, or WebP

---

## 📞 Need Help?

If you encounter issues:
1. Check PHP error log: `C:\xampp\php\logs\php_error_log`
2. Verify directory exists: `C:\xampp\htdocs\Earth_mover\uploads\machines\`
3. Check file permissions on uploads directory
4. Verify XAMPP is running
5. Check database connection



















