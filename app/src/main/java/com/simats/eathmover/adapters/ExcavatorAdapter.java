package com.simats.eathmover.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.eathmover.R;
import com.simats.eathmover.models.Machine;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class ExcavatorAdapter extends RecyclerView.Adapter<ExcavatorAdapter.ExcavatorViewHolder> {

    private List<Machine> excavators;
    private OnExcavatorClickListener listener;

    public interface OnExcavatorClickListener {
        void onExcavatorClick(Machine machine);
    }

    public ExcavatorAdapter(List<Machine> excavators, OnExcavatorClickListener listener) {
        this.excavators = excavators;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExcavatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_excavator, parent, false);
        return new ExcavatorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcavatorViewHolder holder, int position) {
        Machine excavator = excavators.get(position);
        holder.bind(excavator);
    }

    @Override
    public int getItemCount() {
        return excavators != null ? excavators.size() : 0;
    }

    public void updateExcavators(List<Machine> newExcavators) {
        this.excavators = newExcavators;
        notifyDataSetChanged();
    }

    class ExcavatorViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivExcavatorImage;
        private TextView tvExcavatorModel;
        private TextView tvExcavatorPrice;

        public ExcavatorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExcavatorImage = itemView.findViewById(R.id.iv_excavator_image);
            tvExcavatorModel = itemView.findViewById(R.id.tv_excavator_model);
            tvExcavatorPrice = itemView.findViewById(R.id.tv_excavator_price);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onExcavatorClick(excavators.get(position));
                    }
                }
            });
        }

        public void bind(Machine excavator) {
            // Set model name
            String modelName = excavator.getModelName();
            if (modelName != null && !modelName.isEmpty()) {
                tvExcavatorModel.setText(modelName);
            } else {
                tvExcavatorModel.setText("Excavator");
            }

            // Set price
            String priceText = "₹" + String.format(Locale.getDefault(), "%.0f", excavator.getPricePerHour()) + "/hr";
            tvExcavatorPrice.setText(priceText);

            // Load image using Picasso
            // getImage() already prefers machine_image_1 over image
            String imageUrl = excavator.getImage();
            
            // If we have an image URL, use it (API should return full URL)
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // If URL doesn't start with http, construct full URL (fallback)
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    String baseUrl = com.simats.eathmover.config.ApiConfig.getBaseUrl();
                    String rootUrl = baseUrl.replace("/api/", "/");
                    if (imageUrl.startsWith("/")) {
                        imageUrl = imageUrl.substring(1);
                    }
                    imageUrl = rootUrl + imageUrl;
                }
                
                android.util.Log.d("ExcavatorAdapter", "Loading image for machine_id " + excavator.getMachineId() + 
                    " (Model: " + excavator.getModelName() + "): " + imageUrl);
                
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.t110) // Default excavator image
                        .error(R.drawable.t110)
                        .into(ivExcavatorImage);
            } else {
                android.util.Log.w("ExcavatorAdapter", "No image URL for machine_id " + excavator.getMachineId() + 
                    " (Model: " + excavator.getModelName() + ")");
                ivExcavatorImage.setImageResource(R.drawable.t110);
            }
        }
    }
}

