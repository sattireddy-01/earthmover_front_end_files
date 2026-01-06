package com.simats.eathmover.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.eathmover.R;
import com.simats.eathmover.models.Booking;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookings;
    private OnBookingClickListener listener;
    private boolean isOperatorView = false;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
        default void onAcceptClick(Booking booking) {}
        default void onDeclineClick(Booking booking) {}
        default void onCancelClick(Booking booking) {}
        default void onCompleteClick(Booking booking) {}
    }

    // Constructor for Admin (Context + List)
    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
    }

    // Constructor for Operator (List + Listener) - Context will be derived from parent
    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    public void setOperatorView(boolean isOperatorView) {
        this.isOperatorView = isOperatorView;
    }

    // Explicit force mechanism
    private boolean forceUserLayout = false;
    public void setForceUserLayout(boolean force) {
        this.forceUserLayout = force;
    }

    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        
        // Use different layouts based on role
        int layoutId;
        
        // Strict check: If context is explicitly User side, force User Layout
        String contextName = context.getClass().getSimpleName();
        boolean isUserActivity = contextName.contains("UserBookings") || contextName.contains("ServiceHistory");

        if (forceUserLayout || isUserActivity) {
             layoutId = R.layout.item_booking_user;
        } else if (isOperatorView) {
             layoutId = R.layout.item_booking_admin;
        } else {
             // Default fallthrough
             layoutId = R.layout.item_booking_user; 
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        // Pass the effective 'isOperator' flag to ViewHolder
        // If it's User Activity, it is definitely NOT operator view
        return new BookingViewHolder(view, !isUserActivity && isOperatorView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        // Common Bindings
        if (holder.tvMachineName != null) {
            String machineName = booking.getMachineModel();
             if (machineName == null || machineName.isEmpty()) {
                machineName = booking.getMachineType() != null ? booking.getMachineType() : "Unknown Machine";
            }
            holder.tvMachineName.setText(machineName);
        }
        
        if (holder.tvStatus != null) {
            String status = booking.getStatus();
            holder.tvStatus.setText(status);
            
             int statusColor;
            if ("In Progress".equalsIgnoreCase(status) || "Active".equalsIgnoreCase(status) || "Accepted".equalsIgnoreCase(status)) {
                statusColor = ContextCompat.getColor(context, android.R.color.holo_green_dark);
            } else if ("Pending".equalsIgnoreCase(status)) {
                statusColor = ContextCompat.getColor(context, android.R.color.holo_orange_dark); 
            } else if ("Completed".equalsIgnoreCase(status)) {
                statusColor = ContextCompat.getColor(context, android.R.color.holo_blue_dark);
            } else {
                statusColor = ContextCompat.getColor(context, android.R.color.darker_gray);
            }
            
            // Check if we need to tint background (User layout uses backgroundTint for pill)
            if (!isOperatorView) {
                // User layout logic
                 if (holder.tvStatus.getBackground() != null) {
                    holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));
                }
            } else {
                // Admin layout logic (existing)
                if (holder.tvStatus.getBackground() instanceof GradientDrawable) {
                     ((GradientDrawable) holder.tvStatus.getBackground()).setColor(statusColor);
                } else {
                     holder.tvStatus.setBackgroundColor(statusColor);
                }
            }
        }

        if (isOperatorView) {
            // Bind Admin/Operator specific fields
            if (holder.tvBookingId != null) holder.tvBookingId.setText("Booking ID: #" + booking.getBookingId());
            if (holder.tvUserName != null) holder.tvUserName.setText(booking.getUserName() != null ? booking.getUserName() : "N/A");
            if (holder.tvOperatorName != null) holder.tvOperatorName.setText(booking.getOperatorName() != null ? booking.getOperatorName() : "Pending");
             String timeText = (booking.getStartTime() != null ? booking.getStartTime() : "") + 
                          " - " + 
                          (booking.getEndTime() != null ? booking.getEndTime() : "");
            if (holder.tvTime != null) holder.tvTime.setText(timeText);
            if (holder.tvLocation != null) holder.tvLocation.setText(booking.getLocation() != null ? booking.getLocation() : "N/A");
            
        } else {
            // USER VIEW BINDINGS (FUNCTIONALITY)
            
            // 1. Amount
            if (holder.tvAmount != null) {
                holder.tvAmount.setText("₹ " + booking.getTotalAmount());
            }

            // 2. Phone
            if (holder.tvPhone != null) {
                holder.tvPhone.setText(booking.getOperatorPhone() != null ? booking.getOperatorPhone() : "No Contact Info");
            }
            
            // 3. Date
            if (holder.tvDate != null) {
                holder.tvDate.setText(booking.getBookingDate() != null ? booking.getBookingDate() : "Date N/A");
            }
            
            // 4. Operator Name
            if (holder.tvOperatorName != null) {
                 holder.tvOperatorName.setText("Operator: " + (booking.getOperatorName() != null ? booking.getOperatorName() : "Pending"));
            }
            
            // 5. Location
            if (holder.tvLocation != null) {
                holder.tvLocation.setText(booking.getLocation() != null ? booking.getLocation() : "Not specified");
            }

            // 6. Hours
            if (holder.tvHours != null) {
                 holder.tvHours.setText(booking.getTotalHours() + " Hours");
            }

            // 7. Cancel Action
            if (holder.btnCancel != null) {
                String s = booking.getStatus();
                boolean isCancelable = "PENDING".equalsIgnoreCase(s) || "PENDING approval".equalsIgnoreCase(s);
                holder.btnCancel.setVisibility(isCancelable ? View.VISIBLE : View.GONE);
                holder.btnCancel.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelClick(booking);
                });
            }
        }

        // Click Listener for whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    public void updateList(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvMachineName, tvStatus, tvUserName, tvOperatorName, tvTime, tvLocation;
        // New User Fields
        TextView tvAmount, tvPhone, tvDate, tvHours; 
        View btnCancel; // Button
        ImageView ivMachineImage;

        public BookingViewHolder(@NonNull View itemView, boolean isOperator) {
            super(itemView);
            // Common Fields
            tvBookingId = itemView.findViewById(R.id.tv_booking_id); // Might not exist in user layout? Check XML.
            // In item_booking_user we used tv_machine_name, tv_status, etc.
            
            tvMachineName = itemView.findViewById(R.id.tv_machine_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            
            // Layout specific
            if (isOperator) {
                tvBookingId = itemView.findViewById(R.id.tv_booking_id);
                tvUserName = itemView.findViewById(R.id.tv_user_name);
                tvOperatorName = itemView.findViewById(R.id.tv_operator_name);
                tvTime = itemView.findViewById(R.id.tv_time);
                tvLocation = itemView.findViewById(R.id.tv_location);
            } else {
                // User Layout Fields
                tvOperatorName = itemView.findViewById(R.id.tv_operator_name);
                tvAmount = itemView.findViewById(R.id.tv_amount);
                tvPhone = itemView.findViewById(R.id.tv_phone);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvLocation = itemView.findViewById(R.id.tv_location);
                tvHours = itemView.findViewById(R.id.tv_hours);
                btnCancel = itemView.findViewById(R.id.btn_cancel);
            }
            
            ivMachineImage = itemView.findViewById(R.id.iv_machine_image);
        }
    }
}
