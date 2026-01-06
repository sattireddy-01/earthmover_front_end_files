package com.simats.eathmover.retrofit;

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.models.GenericResponse;
import com.simats.eathmover.models.LoginRequest;
import com.simats.eathmover.models.LoginResponse;
import com.simats.eathmover.models.Machine;
import com.simats.eathmover.models.Operator;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.models.OperatorVerification;
import com.simats.eathmover.models.PasswordResetConfirm;
import com.simats.eathmover.models.PasswordResetRequest;
import com.simats.eathmover.models.ReportsData;
import com.simats.eathmover.models.SignUpResponse;
import com.simats.eathmover.models.User;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // User signup endpoint - file is in auth folder
    @POST("auth/user_signup.php")
    Call<SignUpResponse> createUser(@Body User user);

    // Operator signup endpoint
    @POST("auth/operator_signup.php")
    Call<SignUpResponse> createOperator(@Body Operator operator);

    // Admin signup endpoint
    @POST("auth/admin_signup.php")
    Call<SignUpResponse> createAdmin(@Body com.simats.eathmover.models.Admin admin);

    // Request an OTP to be sent to the registered phone number
    // PHP: request_password_reset.php (in api folder, not auth subfolder)
    @POST("request_password_reset.php")
    Call<GenericResponse> requestPasswordReset(@Body PasswordResetRequest request);

    // Confirm OTP and update password
    // PHP: confirm_password_reset.php (in api folder, not auth subfolder)
    @POST("confirm_password_reset.php")
    Call<GenericResponse> confirmPasswordReset(@Body PasswordResetConfirm confirm);

    // User/Operator login
    // PHP: auth/user_login.php
    @POST("auth/user_login.php")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Admin login endpoint
    // PHP: auth/admin_login.php
    @POST("auth/admin_login.php")
    Call<LoginResponse> adminLogin(@Body LoginRequest request);

    // ========== ADMIN ENDPOINTS ==========

    // Get pending operator verifications
    @GET("admin/get_pending_operators.php")
    Call<ApiResponse<OperatorVerification>> getPendingOperators();

    // Get operator verification details by ID
    @GET("admin/get_operator_details.php")
    Call<ApiResponse<OperatorVerification>> getOperatorDetails(@Query("operator_id") String operatorId);

    // Approve operator
    @POST("admin/approve_operator.php")
    Call<GenericResponse> approveOperator(@Body com.simats.eathmover.models.OperatorVerification operator);

    // Reject operator
    @POST("admin/reject_operator.php")
    Call<GenericResponse> rejectOperator(@Body com.simats.eathmover.models.OperatorVerification operator);

    // Get all machines with pricing
    @GET("admin/get_machines.php")
    Call<ApiResponse<Machine>> getMachines();

    // Update machine pricing
    @POST("admin/update_machine_pricing.php")
    Call<GenericResponse> updateMachinePricing(@Body Machine machine);

    // Get live bookings
    @GET("admin/get_live_bookings.php")
    Call<ApiResponse<List<Booking>>> getLiveBookings();

    // Get reports data
    @GET("admin/get_reports.php")
    Call<ApiResponse<ReportsData>> getReports();

    // ========== OPERATOR ENDPOINTS ==========

    // Search available operators (for users)
    @GET("operator/search_operators.php")
    Call<ApiResponse<OperatorProfile>> searchOperators(@Query("location") String location, 
                                                        @Query("machine_type") String machineType,
                                                        @Query("date") String date,
                                                        @Query("time") String time);

    // Get operator profile by ID
    @GET("operator/get_operator_profile.php")
    Call<ApiResponse<OperatorProfile>> getOperatorProfile(@Query("operator_id") String operatorId);

    // Get operator profile details (extended info)
    @GET("operator/get_operator_details.php")
    Call<ApiResponse<OperatorProfile>> getOperatorProfileDetails(@Query("operator_id") String operatorId);

    // Get operator's bookings
    @GET("operator/get_operator_bookings.php")
    Call<ApiResponse<List<Booking>>> getOperatorBookings(@Query("operator_id") String operatorId);

    // Update operator status (available/busy/offline)
    @POST("operator/update_status.php")
    Call<GenericResponse> updateOperatorStatus(@Body Operator operator);

    // Get operator dashboard data
    @GET("operator/get_dashboard.php")
    Call<ApiResponse<OperatorProfile>> getOperatorDashboard(@Query("operator_id") String operatorId);

    // Get new/pending booking requests for operator
    @GET("operator/get_pending_bookings.php")
    Call<ApiResponse<Booking>> getPendingBookings(@Query("operator_id") String operatorId);

    // Accept booking request
    @POST("operator/accept_booking.php")
    Call<GenericResponse> acceptBooking(@Body Booking booking);

    // Decline booking request
    @POST("operator/decline_booking.php")
    Call<GenericResponse> declineBooking(@Body Booking booking);

    // Get operator earnings
    @GET("operator/get_earnings.php")
    Call<ApiResponse<List<Booking>>> getOperatorEarnings(@Query("operator_id") String operatorId);

    // Update operator profile
    @POST("operator/update_profile.php")
    Call<GenericResponse> updateOperatorProfile(@Body OperatorProfile profile);

    // Save operator license details and machine images
    @POST("operator/save_license_details.php")
    Call<GenericResponse> saveLicenseDetails(@Body com.simats.eathmover.models.OperatorLicenseRequest request);

    // ========== USER ENDPOINTS ==========

    // Get all machines for users
    @GET("user/get_machines.php")
    Call<ApiResponse<List<Machine>>> getUserMachines();

    // Get machine details by ID
    @GET("machines/machine_details.php")
    Call<ApiResponse<Machine>> getMachineDetails(@Query("machine_id") int machineId);

    // Get user profile
    @GET("user/get_user_profile.php")
    Call<ApiResponse<User>> getUserProfile(@Query("user_id") String userId);

    // Update user profile
    @POST("user/update_user_profile.php")
    Call<GenericResponse> updateUserProfile(@Body User user);

    // Create booking request
    @POST("booking/create_booking.php")
    Call<GenericResponse> createBooking(@Body Booking booking);

    // Get user bookings (pending and completed)
    @GET("user/get_user_bookings.php")
    Call<ApiResponse<List<Booking>>> getUserBookings(@Query("user_id") String userId);
    // Find operator by selected machine
    @POST("booking/get_operator_by_machine.php")
    Call<ApiResponse<OperatorProfile>> getOperatorByMachine(
            @Body com.simats.eathmover.models.MachineRequest request
    );

    // Mark booking as complete (User side)
    @POST("booking/complete_booking.php")
    Call<GenericResponse> completeBooking(@Body Booking booking);

}
