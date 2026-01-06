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

public class MachineAdapter extends RecyclerView.Adapter<MachineAdapter.MachineViewHolder> {

    private List<Machine> machines;
    private OnMachineClickListener listener;

    public interface OnMachineClickListener {
        void onMachineClick(Machine machine);
    }

    public MachineAdapter(List<Machine> machines, OnMachineClickListener listener) {
        this.machines = machines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MachineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_machine, parent, false);
        return new MachineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MachineViewHolder holder, int position) {
        Machine machine = machines.get(position);
        holder.bind(machine);
    }

    @Override
    public int getItemCount() {
        return machines != null ? machines.size() : 0;
    }

    public void updateMachines(List<Machine> newMachines) {
        this.machines = newMachines;
        notifyDataSetChanged();
    }

    class MachineViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMachineImage;
        private TextView tvModelName;
        private TextView tvSpecs;
        private TextView tvPrice;
        private TextView tvYear;

        public MachineViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMachineImage = itemView.findViewById(R.id.iv_machine_image);
            tvModelName = itemView.findViewById(R.id.tv_model_name);
            tvSpecs = itemView.findViewById(R.id.tv_specs);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvYear = itemView.findViewById(R.id.tv_year);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onMachineClick(machines.get(getAdapterPosition()));
                    }
                }
            });
        }

        public void bind(Machine machine) {
            tvModelName.setText(machine.getModelName());
            tvSpecs.setText(machine.getSpecs());
            tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f/hour", machine.getPricePerHour()));
            
            if (machine.getModelYear() != null) {
                tvYear.setText(String.valueOf(machine.getModelYear()));
                tvYear.setVisibility(View.VISIBLE);
            } else {
                tvYear.setVisibility(View.GONE);
            }

            // Load image using Picasso
            if (machine.getImage() != null && !machine.getImage().isEmpty()) {
                Picasso.get()
                        .load(machine.getImage())
                        .placeholder(R.drawable.earthmover_featured_1)
                        .error(R.drawable.earthmover_featured_1)
                        .into(ivMachineImage);
            } else {
                ivMachineImage.setImageResource(R.drawable.earthmover_featured_1);
            }
        }
    }
}






















