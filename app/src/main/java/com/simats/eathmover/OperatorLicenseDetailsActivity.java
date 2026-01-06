package com.simats.eathmover;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.OperatorLicenseRequest;
import com.simats.eathmover.models.OperatorVerification;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for operator license details and machine information after signup.
 */
public class OperatorLicenseDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OperatorLicenseDetails";
    private static final int REQUEST_IMAGE_1 = 1001;
    private static final int REQUEST_IMAGE_2 = 1002;
    private static final int REQUEST_IMAGE_3 = 1003;
    private static final int REQUEST_CAMERA = 2001;
    private static final int REQUEST_GALLERY = 2002;
    private static final int PERMISSION_CAMERA = 3001;
    private static final int PERMISSION_STORAGE = 3002;

    private EditText etLicenseNumber;
    private EditText etRcNumber;
    private Spinner spinnerEquipmentType;
    private EditText etMachineModel;
    private EditText etMachineYear;
    private ImageView ivPhoto1, ivPhoto2, ivPhoto3;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private String operatorId;
    
    private int currentImageRequest = 0; // Track which image is being selected
    private String image1Base64 = null;
    private String image2Base64 = null;
    private String image3Base64 = null;
    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_license_details);

        Toolbar toolbar = findViewById(R.id.toolbar_license_details);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Get operator ID from intent (from signup)
        Intent intent = getIntent();
        operatorId = intent.getStringExtra("operator_id");
        String phone = intent.getStringExtra("operator_phone");
        
        Log.d(TAG, "=== OperatorLicenseDetailsActivity onCreate ===");
        Log.d(TAG, "Operator ID from intent: " + operatorId);
        Log.d(TAG, "Phone from intent: " + phone);
        
        // Check if operator_id is valid (not null, not empty, and not "0")
        if (operatorId == null || operatorId.isEmpty() || operatorId.equals("0")) {
            Log.w(TAG, "Operator ID is null, empty, or 0. Will try to fetch by phone.");
            
            if (phone != null && !phone.isEmpty()) {
                Log.d(TAG, "Fetching operator ID by phone: " + phone);
                // Try to fetch operator_id by phone
                fetchOperatorIdByPhone(phone);
            } else {
                Log.e(TAG, "Neither operator_id nor operator_phone found in intent");
                Log.w(TAG, "Continuing without operator_id - will try to fetch during submission");
            }
        } else {
            Log.d(TAG, "Operator ID successfully received: " + operatorId);
        }

        // Initialize views
        etLicenseNumber = findViewById(R.id.et_license_number);
        etRcNumber = findViewById(R.id.et_rc_number);
        spinnerEquipmentType = findViewById(R.id.spinner_equipment_type);
        etMachineModel = findViewById(R.id.et_machine_model);
        etMachineYear = findViewById(R.id.et_machine_year);
        ivPhoto1 = findViewById(R.id.iv_photo_1);
        ivPhoto2 = findViewById(R.id.iv_photo_2);
        ivPhoto3 = findViewById(R.id.iv_photo_3);
        btnSubmit = findViewById(R.id.btn_submit_license);
        progressBar = findViewById(R.id.progress_bar);

        // Set up Equipment Type Spinner
        if (spinnerEquipmentType != null) {
            // Create adapter with "Equipment Type" as first item (hint)
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item_equipment,
                android.R.id.text1
            ) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (view != null) {
                        android.widget.TextView textView = view.findViewById(android.R.id.text1);
                        if (textView != null) {
                            if (position == 0) {
                                // Show "Equipment Type" as hint for first item
                                textView.setText("Equipment Type");
                                textView.setTextColor(0xFFC0C0C0); // Gray color for hint
                            } else {
                                textView.setTextColor(0xFFFFFFFF); // White color for options
                            }
                        }
                    }
                    return view;
                }
            };
            
            // Add "Equipment Type" as first item (hint)
            adapter.add("Equipment Type");
            // Add actual equipment types
            String[] equipmentTypes = getResources().getStringArray(R.array.equipment_types);
            for (String type : equipmentTypes) {
                adapter.add(type);
            }
            
            adapter.setDropDownViewResource(R.layout.spinner_item_equipment);
            spinnerEquipmentType.setAdapter(adapter);
            spinnerEquipmentType.setSelection(0); // Set "Equipment Type" as default
        }

        // Set up photo click listeners (for uploading images)
        if (ivPhoto1 != null) {
            ivPhoto1.setOnClickListener(v -> showImageSourceDialog(REQUEST_IMAGE_1));
        }

        if (ivPhoto2 != null) {
            ivPhoto2.setOnClickListener(v -> showImageSourceDialog(REQUEST_IMAGE_2));
        }

        if (ivPhoto3 != null) {
            ivPhoto3.setOnClickListener(v -> showImageSourceDialog(REQUEST_IMAGE_3));
        }

        // Submit button
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateInput()) {
                        submitLicenseDetails();
                    }
                }
            });
        }
    }

    private boolean validateInput() {
        String licenseNumber = etLicenseNumber.getText().toString().trim();
        String rcNumber = etRcNumber.getText().toString().trim();
        String machineModel = etMachineModel.getText().toString().trim();
        String machineYear = etMachineYear.getText().toString().trim();

        if (licenseNumber.isEmpty()) {
            etLicenseNumber.setError("License Number is required");
            etLicenseNumber.requestFocus();
            return false;
        }

        if (rcNumber.isEmpty()) {
            etRcNumber.setError("RC Number is required");
            etRcNumber.requestFocus();
            return false;
        }

        if (machineModel.isEmpty()) {
            etMachineModel.setError("Machine Model is required");
            etMachineModel.requestFocus();
            return false;
        }

        if (machineYear.isEmpty()) {
            etMachineYear.setError("Machine Year is required");
            etMachineYear.requestFocus();
            return false;
        }

        // Validate equipment type is selected
        if (spinnerEquipmentType != null) {
            int selectedPosition = spinnerEquipmentType.getSelectedItemPosition();
            if (selectedPosition == 0) { // Position 0 is "Equipment Type" hint
                Toast.makeText(this, "Please select Equipment Type", Toast.LENGTH_SHORT).show();
                spinnerEquipmentType.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void submitLicenseDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (btnSubmit != null) btnSubmit.setEnabled(false);

        String licenseNumber = etLicenseNumber.getText().toString().trim();
        String rcNumber = etRcNumber.getText().toString().trim();
        String machineModel = etMachineModel.getText().toString().trim();
        String machineYear = etMachineYear.getText().toString().trim();
        
        // Get selected equipment type from spinner
        String equipmentType = "";
        if (spinnerEquipmentType != null) {
            int selectedPosition = spinnerEquipmentType.getSelectedItemPosition();
            if (selectedPosition > 0) { // Position 0 is "Equipment Type" hint
                equipmentType = spinnerEquipmentType.getSelectedItem().toString();
                Log.d(TAG, "Selected equipment type: " + equipmentType);
            } else {
                Log.w(TAG, "Equipment type not selected (still on hint)");
            }
        }

        // Check if operator_id is valid (not null, not empty, and not "0")
        if (operatorId == null || operatorId.isEmpty() || operatorId.equals("0")) {
            // Try to get phone from intent and fetch operator_id
            Intent intent = getIntent();
            String phone = intent.getStringExtra("operator_phone");
            
            if (phone != null && !phone.isEmpty()) {
                Toast.makeText(this, "Fetching operator information...", Toast.LENGTH_SHORT).show();
                fetchOperatorIdByPhone(phone);
                // Don't return - let the fetchOperatorIdByPhone handle it
                // But we need to prevent submission until operator_id is found
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSubmit != null) btnSubmit.setEnabled(true);
                return;
            } else {
                Toast.makeText(this, "Operator ID not found. Please try signing up again.", Toast.LENGTH_LONG).show();
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSubmit != null) btnSubmit.setEnabled(true);
                return;
            }
        }

        // Create request object
        OperatorLicenseRequest request = new OperatorLicenseRequest();
        request.setOperatorId(operatorId);
        request.setLicenseNo(licenseNumber);
        request.setRcNumber(rcNumber);
        request.setEquipmentType(equipmentType);
        request.setMachineModel(machineModel);
        request.setMachineYear(machineYear);
        request.setMachineImage1(image1Base64);
        // machine_image_2 and machine_image_3 removed from database
        
        Log.d(TAG, "Submitting license details - Equipment Type: " + equipmentType);

        // Call API to save license details
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<GenericResponse> call = apiService.saveLicenseDetails(request);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSubmit != null) btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse apiResponse = response.body();
                    Log.d(TAG, "API Response - Success: " + apiResponse.isSuccess() + ", Message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(OperatorLicenseDetailsActivity.this,
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "License details submitted successfully",
                            Toast.LENGTH_SHORT).show();
                        
                        Log.d(TAG, "Navigating to waiting approval page with operator_id: " + operatorId);
                        
                        // Navigate to waiting for approval page
                        Intent intent = new Intent(OperatorLicenseDetailsActivity.this, OperatorWaitingApprovalActivity.class);
                        intent.putExtra("operator_id", operatorId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to submit license details";
                        Toast.makeText(OperatorLicenseDetailsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "API returned failure: " + errorMsg);
                    }
                } else {
                    String errorMsg = "Failed to submit license details. Error code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += " - " + errorBody;
                            Log.e(TAG, "Error response body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(OperatorLicenseDetailsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to submit license details: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSubmit != null) btnSubmit.setEnabled(true);
                
                String errorMsg = "Network error";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("JsonReader") || t.getMessage().contains("lenient")) {
                        errorMsg = "Server response error. Please try again.";
                        Log.e(TAG, "JSON parsing error - this might be due to malformed server response", t);
                    } else {
                        errorMsg = "Network error: " + t.getMessage();
                    }
                }
                
                Toast.makeText(OperatorLicenseDetailsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error submitting license details: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Show dialog to choose image source (Camera or Gallery)
     */
    private void showImageSourceDialog(int imageRequest) {
        currentImageRequest = imageRequest;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
            if (which == 0) {
                // Take Photo
                if (checkCameraPermission()) {
                    openCamera();
                }
            } else {
                // Choose from Gallery
                if (checkStoragePermission()) {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    /**
     * Check and request camera permission
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            return false;
        }
        return true;
    }

    /**
     * Check and request storage permission
     */
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_STORAGE);
                return false;
            }
        } else {
            // Android 12 and below uses READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Open camera to take photo
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create a file to save the image
            File photoFile = new File(getExternalFilesDir(null), "temp_camera_image.jpg");
            cameraImageUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".fileprovider",
                photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open gallery to choose image
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSION_CAMERA) {
                openCamera();
            } else if (requestCode == PERMISSION_STORAGE) {
                openGallery();
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            
            if (requestCode == REQUEST_CAMERA && cameraImageUri != null) {
                // Image from camera
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cameraImageUri));
                } catch (Exception e) {
                    Log.e(TAG, "Error loading camera image: " + e.getMessage());
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
                // Image from gallery
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading gallery image: " + e.getMessage());
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            if (bitmap != null) {
                // Resize bitmap to reduce size (max 1024px)
                bitmap = resizeBitmap(bitmap, 1024);
                
                // Convert to Base64
                String base64Image = bitmapToBase64(bitmap);
                
                // Update the appropriate ImageView and store Base64
                switch (currentImageRequest) {
                    case REQUEST_IMAGE_1:
                        if (ivPhoto1 != null) ivPhoto1.setImageBitmap(bitmap);
                        image1Base64 = base64Image;
                        break;
                    case REQUEST_IMAGE_2:
                        if (ivPhoto2 != null) ivPhoto2.setImageBitmap(bitmap);
                        image2Base64 = base64Image;
                        break;
                    case REQUEST_IMAGE_3:
                        if (ivPhoto3 != null) ivPhoto3.setImageBitmap(bitmap);
                        image3Base64 = base64Image;
                        break;
                }
                
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Resize bitmap to reduce file size
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }
        
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Convert bitmap to Base64 string
     */
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // 80% quality
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    /**
     * Fetch operator_id by phone number from pending operators list
     */
    private void fetchOperatorIdByPhone(String phone) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (btnSubmit != null) btnSubmit.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse<OperatorVerification>> call = apiService.getPendingOperators();

        call.enqueue(new Callback<ApiResponse<OperatorVerification>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorVerification>> call, Response<ApiResponse<OperatorVerification>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSubmit != null) btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiResponse<OperatorVerification> apiResponse = response.body();
                    if (apiResponse.getDataList() != null) {
                        // Search for operator with matching phone
                        for (OperatorVerification operator : apiResponse.getDataList()) {
                            if (phone.equals(operator.getPhone())) {
                                operatorId = operator.getOperatorId();
                                Log.d(TAG, "Found operator_id by phone: " + operatorId);
                                Toast.makeText(OperatorLicenseDetailsActivity.this, 
                                    "Operator information loaded successfully", 
                                    Toast.LENGTH_SHORT).show();
                                // Re-enable submit button now that we have operator_id
                                if (btnSubmit != null) btnSubmit.setEnabled(true);
                                return;
                            }
                        }
                    }
                    // If not found in pending list, log warning but don't show error
                    // The operator was just created, so it might not be in pending list yet
                    Log.w(TAG, "Operator not found in pending operators list with phone: " + phone);
                    Log.w(TAG, "This is normal for newly created operators. Continuing without operator_id.");
                    Toast.makeText(OperatorLicenseDetailsActivity.this, 
                        "Operator ID not found yet, but you can continue filling the form", 
                        Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to fetch pending operators: " + response.code());
                    // Don't show error - just log and continue
                    Log.w(TAG, "Continuing without operator_id - will try during form submission");
                    Toast.makeText(OperatorLicenseDetailsActivity.this, 
                        "Could not fetch operator ID, but you can continue filling the form", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorVerification>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSubmit != null) btnSubmit.setEnabled(true);
                Log.e(TAG, "Error fetching operator by phone: " + t.getMessage());
                // Don't show error - just log and continue
                Log.w(TAG, "Continuing without operator_id - will try during form submission");
                Toast.makeText(OperatorLicenseDetailsActivity.this, 
                    "Network error, but you can continue filling the form", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show error dialog when operator_id cannot be found
     * Modified to not automatically finish the activity
     */
    private void showOperatorIdError() {
        new AlertDialog.Builder(this)
            .setTitle("Operator ID Not Found")
            .setMessage("Unable to retrieve your operator information automatically. You can still fill out the form - we'll try to find your operator ID when you submit.")
            .setPositiveButton("Continue", (dialog, which) -> {
                // Don't finish - let user continue filling the form
                Toast.makeText(this, "You can continue filling the form", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Retry", (dialog, which) -> {
                // Retry by checking intent again
                Intent intent = getIntent();
                operatorId = intent.getStringExtra("operator_id");
                String phone = intent.getStringExtra("operator_phone");
                if (operatorId == null && phone != null) {
                    fetchOperatorIdByPhone(phone);
                } else if (operatorId != null) {
                    Toast.makeText(this, "Operator ID found: " + operatorId, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please continue filling the form", Toast.LENGTH_SHORT).show();
                }
            })
            .setCancelable(true)
            .show();
    }
}





