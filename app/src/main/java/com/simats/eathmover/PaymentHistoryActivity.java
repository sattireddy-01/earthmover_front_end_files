package com.simats.eathmover;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity showing payment history with list of past transactions.
 */
public class PaymentHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PaymentHistoryAdapter adapter;
    private List<PaymentHistoryItem> paymentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        Toolbar toolbar = findViewById(R.id.toolbar_payment_history);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rv_payment_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample payment data
        paymentList = new ArrayList<>();
        paymentList.add(new PaymentHistoryItem("Crane Booking", "12/08/2024", "$150", R.drawable.ic_crane));
        paymentList.add(new PaymentHistoryItem("Excavator Rental", "11/24/2024", "$200", R.drawable.ic_excavator));
        paymentList.add(new PaymentHistoryItem("JCB Booking", "11/23/2024", "$180", R.drawable.ic_jcb));
        paymentList.add(new PaymentHistoryItem("Dozer Rental", "11/21/2024", "$220", R.drawable.ic_dozer));
        paymentList.add(new PaymentHistoryItem("Crane Booking", "11/16/2024", "$160", R.drawable.ic_crane));
        paymentList.add(new PaymentHistoryItem("Excavator Rental", "11/09/2024", "$190", R.drawable.ic_excavator));

        adapter = new PaymentHistoryAdapter(paymentList);
        recyclerView.setAdapter(adapter);

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_payment_history);
        if (bottomNav != null) {
            com.simats.eathmover.utils.BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
            bottomNav.setSelectedItemId(R.id.navigation_history);
        }
    }

    /**
     * Data model for payment history items.
     */
    private static class PaymentHistoryItem {
        String bookingType;
        String date;
        String amount;
        int iconResId;

        PaymentHistoryItem(String bookingType, String date, String amount, int iconResId) {
            this.bookingType = bookingType;
            this.date = date;
            this.amount = amount;
            this.iconResId = iconResId;
        }
    }

    /**
     * RecyclerView adapter for payment history list.
     */
    private class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder> {
        private List<PaymentHistoryItem> items;

        PaymentHistoryAdapter(List<PaymentHistoryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_payment_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PaymentHistoryItem item = items.get(position);
            holder.bookingType.setText(item.bookingType);
            holder.date.setText(item.date);
            holder.amount.setText(item.amount);
            holder.icon.setImageResource(item.iconResId);

            holder.itemView.setOnClickListener(v -> {
                // TODO: Open payment details if needed
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView bookingType;
            TextView date;
            TextView amount;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.iv_payment_icon);
                bookingType = itemView.findViewById(R.id.tv_booking_type);
                date = itemView.findViewById(R.id.tv_payment_date);
                amount = itemView.findViewById(R.id.tv_payment_amount);
            }
        }
    }
}

