package com.example.parksmart.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parksmart.R;
import com.example.parksmart.models.home.ParkingUiModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ParkingCardAdapter extends RecyclerView.Adapter<ParkingCardAdapter.ParkingViewHolder> {

    private final List<ParkingUiModel> items = new ArrayList<>();
    private final Listener listener;

    public ParkingCardAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<ParkingUiModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_parking, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        ParkingUiModel item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvDistance.setText(item.getDistanceText());
        holder.tvAvailability.setText(item.getAvailabilityText());
        holder.tvPrediction.setText(item.getPredictionText());
        holder.tvDetails.setText(item.getDetailsText());
        holder.tvAddress.setText(item.getAddress() == null || item.getAddress().isEmpty() ? "Adresse indisponible" : item.getAddress());

        holder.btnSave.setColorFilter(holder.itemView.getContext().getColor(item.isSaved() ? R.color.ps_primary_dark : R.color.ps_primary));
        holder.btnSave.setOnClickListener(v -> listener.onToggleSaved(item));
        holder.btnDetails.setOnClickListener(v -> listener.onShowDetails(item));
        holder.card.setOnClickListener(v -> listener.onShowDetails(item));

        Integer predicted = item.getPredictedFreePlaces();
        int predictionColor = predicted == null ? R.color.ps_text_muted : (predicted > 0 ? R.color.ps_success : R.color.ps_error);
        holder.tvPrediction.setTextColor(holder.itemView.getContext().getColor(predictionColor));
        holder.badgeRecommended.setVisibility(item.isRecommended() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ParkingViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView tvName;
        private final TextView tvDistance;
        private final TextView tvAvailability;
        private final TextView tvPrediction;
        private final TextView tvDetails;
        private final TextView tvAddress;
        private final TextView badgeRecommended;
        private final ImageButton btnSave;
        private final ImageButton btnDetails;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            tvName = itemView.findViewById(R.id.tvName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            tvPrediction = itemView.findViewById(R.id.tvPrediction);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            badgeRecommended = itemView.findViewById(R.id.tvRecommendedBadge);
            btnSave = itemView.findViewById(R.id.btnSave);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }

    public interface Listener {
        void onToggleSaved(ParkingUiModel item);
        void onShowDetails(ParkingUiModel item);
    }
}
