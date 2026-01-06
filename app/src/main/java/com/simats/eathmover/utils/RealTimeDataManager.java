package com.simats.eathmover.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.simats.eathmover.models.ApiResponse;
import com.simats.eathmover.models.Booking;
import com.simats.eathmover.models.OperatorProfile;
import com.simats.eathmover.retrofit.ApiService;
import com.simats.eathmover.retrofit.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manager for real-time data updates using polling mechanism.
 * Polls API at regular intervals and notifies listeners of data changes.
 */
public class RealTimeDataManager {
    private static final String TAG = "RealTimeDataManager";
    private static final long POLLING_INTERVAL = 10000; // 10 seconds
    private static final long FAST_POLLING_INTERVAL = 5000; // 5 seconds for active screens

    private static RealTimeDataManager instance;
    private Handler handler;
    private ApiService apiService;
    private SessionManager sessionManager;

    // Polling state
    private boolean isPolling = false;
    private Runnable pollingRunnable;
    private long currentPollingInterval = POLLING_INTERVAL;

    // Listeners
    private DashboardDataListener dashboardListener;
    private BookingRequestListener bookingRequestListener;
    private EarningsDataListener earningsListener;
    private BookingStatusListener bookingStatusListener;
    private UserBookingsListener userBookingsListener;
    private OperatorBookingsListener operatorBookingsListener;

    private RealTimeDataManager() {
        handler = new Handler(Looper.getMainLooper());
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public static synchronized RealTimeDataManager getInstance() {
        if (instance == null) {
            instance = new RealTimeDataManager();
        }
        return instance;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Start polling for real-time updates
     */
    public void startPolling() {
        if (isPolling) {
            return;
        }

        isPolling = true;
        currentPollingInterval = POLLING_INTERVAL;
        scheduleNextPoll();
        Log.d(TAG, "Real-time polling started");
    }

    /**
     * Start fast polling (for active screens like booking requests)
     */
    public void startFastPolling() {
        if (isPolling) {
            currentPollingInterval = FAST_POLLING_INTERVAL;
        } else {
            isPolling = true;
            currentPollingInterval = FAST_POLLING_INTERVAL;
            scheduleNextPoll();
        }
        Log.d(TAG, "Fast polling started");
    }

    /**
     * Stop polling
     */
    public void stopPolling() {
        isPolling = false;
        if (pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
        Log.d(TAG, "Real-time polling stopped");
    }

    private void scheduleNextPoll() {
        if (!isPolling) {
            return;
        }

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPolling && sessionManager != null) {
                    String operatorId = sessionManager.getOperatorId();
                    if (operatorId != null) {
                        pollDashboardData(operatorId);
                        pollPendingBookings(operatorId);
                        pollEarnings(operatorId);
                        pollCurrentBooking(operatorId);
                        pollOperatorBookings(operatorId);
                    }
                    String userId = sessionManager.getUserId();
                    if (userId != null) {
                        pollUserBookings(userId);
                    }
                    scheduleNextPoll();
                }
            }
        };

        handler.postDelayed(pollingRunnable, currentPollingInterval);
    }

    // Dashboard Data Polling
    private void pollDashboardData(String operatorId) {
        if (dashboardListener == null) return;

        Call<ApiResponse<OperatorProfile>> call = apiService.getOperatorDashboard(operatorId);
        call.enqueue(new Callback<ApiResponse<OperatorProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperatorProfile>> call, Response<ApiResponse<OperatorProfile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OperatorProfile> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        dashboardListener.onDashboardDataUpdated(apiResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperatorProfile>> call, Throwable t) {
                Log.e(TAG, "Error polling dashboard data: " + t.getMessage());
            }
        });
    }

    // Pending Bookings Polling
    private void pollPendingBookings(String operatorId) {
        if (bookingRequestListener == null) return;

        Call<ApiResponse<Booking>> call = apiService.getPendingBookings(operatorId);
        call.enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getDataList() != null) {
                        bookingRequestListener.onNewBookingRequest(apiResponse.getDataList());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                Log.e(TAG, "Error polling pending bookings: " + t.getMessage());
            }
        });
    }

    // Earnings Polling
    private void pollEarnings(String operatorId) {
        if (earningsListener == null) return;

        Call<ApiResponse<List<Booking>>> call = apiService.getOperatorEarnings(operatorId);
        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    // data field is T, which is List<Booking>.
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        earningsListener.onEarningsUpdated(apiResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e(TAG, "Error polling earnings: " + t.getMessage());
            }
        });
    }

    // Current Booking Status Polling
    private void pollCurrentBooking(String operatorId) {
        if (bookingStatusListener == null) return;

        Call<ApiResponse<List<Booking>>> call = apiService.getOperatorBookings(operatorId);
        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // Find active booking
                        for (Booking booking : apiResponse.getData()) {
                            if ("active".equalsIgnoreCase(booking.getStatus()) ||
                                "in_progress".equalsIgnoreCase(booking.getStatus())) {
                                bookingStatusListener.onBookingStatusChanged(booking);
                                return;
                            }
                        }
                        // No active booking
                        bookingStatusListener.onBookingStatusChanged(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e(TAG, "Error polling booking status: " + t.getMessage());
            }
        });
    }

    // User Bookings Polling
    private void pollUserBookings(String userId) {
        if (userBookingsListener == null) return;

        Call<ApiResponse<List<Booking>>> call = apiService.getUserBookings(userId);
        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        userBookingsListener.onUserBookingsUpdated(apiResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e(TAG, "Error polling user bookings: " + t.getMessage());
            }
        });
    }

    // Operator Bookings Polling
    private void pollOperatorBookings(String operatorId) {
        if (operatorBookingsListener == null) return;

        Call<ApiResponse<List<Booking>>> call = apiService.getOperatorBookings(operatorId);
        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        operatorBookingsListener.onOperatorBookingsUpdated(apiResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e(TAG, "Error polling operator bookings: " + t.getMessage());
            }
        });
    }

    // Listener Interfaces
    public interface DashboardDataListener {
        void onDashboardDataUpdated(OperatorProfile profile);
    }

    public interface BookingRequestListener {
        void onNewBookingRequest(List<Booking> bookings);
    }

    public interface EarningsDataListener {
        void onEarningsUpdated(List<Booking> transactions);
    }

    public interface BookingStatusListener {
        void onBookingStatusChanged(Booking booking);
    }

    public interface UserBookingsListener {
        void onUserBookingsUpdated(List<Booking> bookings);
    }

    public interface OperatorBookingsListener {
        void onOperatorBookingsUpdated(List<Booking> bookings);
    }

    // Set Listeners
    public void setDashboardListener(DashboardDataListener listener) {
        this.dashboardListener = listener;
    }

    public void setBookingRequestListener(BookingRequestListener listener) {
        this.bookingRequestListener = listener;
    }

    public void setEarningsListener(EarningsDataListener listener) {
        this.earningsListener = listener;
    }

    public void setBookingStatusListener(BookingStatusListener listener) {
        this.bookingStatusListener = listener;
    }

    public void setUserBookingsListener(UserBookingsListener listener) {
        this.userBookingsListener = listener;
    }

    public void setOperatorBookingsListener(OperatorBookingsListener listener) {
        this.operatorBookingsListener = listener;
    }

    // Remove Listeners
    public void removeDashboardListener() {
        this.dashboardListener = null;
    }

    public void removeBookingRequestListener() {
        this.bookingRequestListener = null;
    }

    public void removeEarningsListener() {
        this.earningsListener = null;
    }

    public void removeBookingStatusListener() {
        this.bookingStatusListener = null;
    }

    public void removeUserBookingsListener() {
        this.userBookingsListener = null;
    }

    public void removeOperatorBookingsListener() {
        this.operatorBookingsListener = null;
    }

    /**
     * Manually trigger immediate data refresh
     */
    public void refreshNow() {
        if (sessionManager != null) {
            String operatorId = sessionManager.getOperatorId();
            if (operatorId != null) {
                pollDashboardData(operatorId);
                pollPendingBookings(operatorId);
                pollEarnings(operatorId);
                pollCurrentBooking(operatorId);
                pollOperatorBookings(operatorId);
            }
            String userId = sessionManager.getUserId();
            if (userId != null) {
                pollUserBookings(userId);
            }
        }
    }
}
