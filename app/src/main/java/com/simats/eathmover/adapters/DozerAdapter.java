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

public class DozerAdapter extends RecyclerView.Adapter<DozerAdapter.DozerViewHolder> {

    private List<Machine> dozers;
    private OnDozerClickListener listener;

    public interface OnDozerClickListener {
        void onDozerClick(Machine machine);
    }

    public DozerAdapter(List<Machine> dozers, OnDozerClickListener listener) {
        this.dozers = dozers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DozerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dozer, parent, false);
        return new DozerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DozerViewHolder holder, int position) {
        Machine dozer = dozers.get(position);
        holder.bind(dozer);
    }

    @Override
    public int getItemCount() {
        return dozers != null ? dozers.size() : 0;
    }

    public void updateDozers(List<Machine> newDozers) {
        this.dozers = newDozers;
        notifyDataSetChanged();
    }

    class DozerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivDozerImage;
        private TextView tvDozerModel;
        private TextView tvDozerPrice;

        public DozerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDozerImage = itemView.findViewById(R.id.iv_dozer_image);
            tvDozerModel = itemView.findViewById(R.id.tv_dozer_model);
            tvDozerPrice = itemView.findViewById(R.id.tv_dozer_price);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDozerClick(dozers.get(position));
                    }
                }
            });
        }

        public void bind(Machine dozer) {
            // Set model name
            String modelName = dozer.getModelName();
            if (modelName != null && !modelName.isEmpty()) {
                tvDozerModel.setText(modelName);
            } else {
                tvDozerModel.setText("Dozer");
            }

            // Set price
            String priceText = "₹" + String.format(Locale.getDefault(), "%.0f", dozer.getPricePerHour()) + "/hr";
            tvDozerPrice.setText(priceText);

            // Load image using Picasso
            String imageUrl = dozer.getImage();
            
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
                
                android.util.Log.d("DozerAdapter", "Loading image for machine_id " + dozer.getMachineId() + 
                    " (Model: " + dozer.getModelName() + "): " + imageUrl);
                
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.dozer) // Default dozer image
                        .error(R.drawable.dozer)
                        .into(ivDozerImage);
            } else {
                android.util.Log.w("DozerAdapter", "No image URL for machine_id " + dozer.getMachineId() + 
                    " (Model: " + dozer.getModelName() + ")");
                ivDozerImage.setImageResource(R.drawable.dozer);
            }
        }
    }
}








