# Postman Guide: Upload Machine Data

## 📋 Quick Setup Checklist

- [ ] Method: **POST**
- [ ] Body Type: **raw** (NOT form-data)
- [ ] Body Format: **JSON** (from dropdown)
- [ ] Header: **Content-Type: application/json**

---

## 🆕 Creating a New Machine

### Step-by-Step:

1. **Open Postman**
   - Click "New" → "HTTP Request"

2. **Set Method and URL**
   - Method: Select **POST** from dropdown
   - URL: `http://localhost/Earth_mover/api/admin/create_machine.php`

3. **Add Header**
   - Click **Headers** tab
   - Add:
     - Key: `Content-Type`
     - Value: `application/json`
   - ✅ Make sure it's checked/enabled

4. **Configure Body**
   - Click **Body** tab
   - Select **raw** radio button (NOT form-data, NOT x-www-form-urlencoded)
   - In the dropdown next to "raw", select **JSON** (important!)
   - Paste this JSON:

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

5. **Send Request**
   - Click the blue **Send** button
   - You should see a success response with `machine_id`

---

## ✏️ Updating Existing Machine Price

### Step-by-Step:

1. **Set Method and URL**
   - Method: **POST**
   - URL: `http://localhost/Earth_mover/api/admin/update_machine_pricing.php`

2. **Add Header**
   - Headers tab
   - `Content-Type: application/json`

3. **Configure Body**
   - Body tab → **raw** → **JSON**
   - Paste this JSON:

```json
{
  "machine_id": 1,
  "price_per_hour": 1300.00
}
```

4. **Send Request**

---

## 📝 Complete JSON Examples

### Example 1: Create Machine (Minimal)
```json
{
  "category_id": 2,
  "model_name": "Tata Hitachi EX 110",
  "price_per_hour": 1400.00
}
```

### Example 2: Create Machine (Complete)
```json
{
  "category_id": 3,
  "model_name": "John Deere 5050D",
  "price_per_hour": 1200.00,
  "specs": "Heavy duty dozer for large construction projects",
  "model_year": 2024,
  "image": "uploads/machines/john_deere_5050d.jpg"
}
```

### Example 3: Update Machine Price
```json
{
  "machine_id": 1,
  "price_per_hour": 1350.00
}
```

---

## ⚠️ Common Mistakes to Avoid

### ❌ WRONG: Using form-data
- Don't use the "form-data" option
- The PHP script reads JSON, not form data

### ❌ WRONG: Wrong Content-Type
- Don't use `application/x-www-form-urlencoded`
- Must be `application/json`

### ❌ WRONG: Text instead of JSON
- Don't select "Text" in the raw dropdown
- Must select "JSON"

### ❌ WRONG: Trailing commas
```json
{
  "category_id": 1,
  "model_name": "JCB 3DX",  // ← Remove this comma
}
```

### ❌ WRONG: Single quotes
```json
{
  'category_id': 1,  // ← Use double quotes
  'model_name': 'JCB 3DX'
}
```

### ❌ WRONG: Numbers in quotes
```json
{
  "category_id": "1",  // ← Remove quotes from numbers
  "price_per_hour": "1250.00"
}
```

---

## ✅ Correct Format

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

---

## 🔍 Expected Responses

### Success Response (Create):
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
    "specs": "Backhoe Loader",
    "model_year": 2024,
    "image": null
  }
}
```

### Success Response (Update):
```json
{
  "success": true,
  "ok": true,
  "message": "Machine pricing updated successfully",
  "machine_id": 1,
  "price_per_hour": 1300.00
}
```

### Error Response:
```json
{
  "success": false,
  "message": "category_id is required and must be greater than 0"
}
```

---

## 🐛 Troubleshooting

### Error: "Invalid JSON: Syntax error"
- ✅ Check you selected **raw** → **JSON** (not Text)
- ✅ Check for trailing commas
- ✅ Check all quotes are double quotes `"`
- ✅ Validate JSON at: https://jsonlint.com/

### Error: "machine_id is required"
- ✅ For creating: Don't include `machine_id` (it's auto-generated)
- ✅ For updating: Must include `machine_id` with value > 0

### Error: "category_id is required"
- ✅ Make sure `category_id` is a number (not in quotes)
- ✅ Value must be > 0

### Error: "Database connection error"
- ✅ Make sure XAMPP MySQL is running
- ✅ Check database name is `earthmover`
- ✅ Verify `config/database.php` exists

---

## 📸 Visual Guide

**Body Tab Configuration:**
```
┌─────────────────────────────────────┐
│ Body                                │
├─────────────────────────────────────┤
│ ○ none                              │
│ ○ form-data                         │
│ ○ x-www-form-urlencoded             │
│ ● raw  [JSON ▼]  ← Select this!    │
│ ○ binary                            │
│ ○ GraphQL                           │
├─────────────────────────────────────┤
│ {                                   │
│   "category_id": 1,                 │
│   "model_name": "JCB 3DX",          │
│   ...                               │
│ }                                   │
└─────────────────────────────────────┘
```

**Headers Tab:**
```
┌─────────────────────────────────────┐
│ Headers                             │
├─────────────────────────────────────┤
│ Content-Type  application/json  ✓   │
└─────────────────────────────────────┘
```

---

## 🎯 Quick Test

1. Copy this exact JSON:
```json
{
  "category_id": 1,
  "model_name": "Test Machine",
  "price_per_hour": 1000.00
}
```

2. In Postman:
   - Method: POST
   - URL: `http://localhost/Earth_mover/api/admin/create_machine.php`
   - Headers: `Content-Type: application/json`
   - Body: raw → JSON → paste above JSON
   - Click Send

3. Expected: Success with `machine_id` in response



















