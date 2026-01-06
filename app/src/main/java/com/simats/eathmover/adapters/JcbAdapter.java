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

public class JcbAdapter extends RecyclerView.Adapter<JcbAdapter.JcbViewHolder> {

    private List<Machine> jcbs;
    private OnJcbClickListener listener;

    public interface OnJcbClickListener {
        void onJcbClick(Machine machine);
    }

    public JcbAdapter(List<Machine> jcbs, OnJcbClickListener listener) {
        this.jcbs = jcbs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JcbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_jcb, parent, false);
        return new JcbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JcbViewHolder holder, int position) {
        Machine jcb = jcbs.get(position);
        holder.bind(jcb);
    }

    @Override
    public int getItemCount() {
        return jcbs != null ? jcbs.size() : 0;
    }

    public void updateJcbs(List<Machine> newJcbs) {
        this.jcbs = newJcbs;
        notifyDataSetChanged();
    }

    class JcbViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivJcbImage;
        private TextView tvJcbModel;
        private TextView tvJcbPrice;

        public JcbViewHolder(@NonNull View itemView) {
            super(itemView);
            ivJcbImage = itemView.findViewById(R.id.iv_jcb_image);
            tvJcbModel = itemView.findViewById(R.id.tv_jcb_model);
            tvJcbPrice = itemView.findViewById(R.id.tv_jcb_price);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onJcbClick(jcbs.get(position));
                    }
                }
            });
        }

        public void bind(Machine jcb) {
            // Set model name
            String modelName = jcb.getModelName();
            if (modelName != null && !modelName.isEmpty()) {
                tvJcbModel.setText(modelName);
            } else {
                tvJcbModel.setText("JCB");
            }

            // Set price
            String priceText = "₹" + String.format(Locale.getDefault(), "%.0f", jcb.getPricePerHour()) + "/hr";
            tvJcbPrice.setText(priceText);

            // Load image using Picasso
            String imageUrl = jcb.getImage();
            
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
                
                android.util.Log.d("JcbAdapter", "Loading image for machine_id " + jcb.getMachineId() + 
                    " (Model: " + jcb.getModelName() + "): " + imageUrl);
                
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.jcb3dx_1) // Default JCB image
                        .error(R.drawable.jcb3dx_1)
                        .into(ivJcbImage);
            } else {
                android.util.Log.w("JcbAdapter", "No image URL for machine_id " + jcb.getMachineId() + 
                    " (Model: " + jcb.getModelName() + ")");
                ivJcbImage.setImageResource(R.drawable.jcb3dx_1);
            }
        }
    }
}








