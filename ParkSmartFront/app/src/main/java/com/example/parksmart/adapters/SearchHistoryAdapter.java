package com.example.parksmart.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parksmart.R;
import com.example.parksmart.models.home.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.HistoryViewHolder> {

    private final List<HistoryItem> items = new ArrayList<>();
    private final Listener listener;

    public SearchHistoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<HistoryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        holder.tvQuery.setText(item.getDestinationText());
        String recommended = item.getRecommendedParking() == null || item.getRecommendedParking().isEmpty()
                ? "Dernière recherche"
                : "Recommandé : " + item.getRecommendedParking();
        String arrival = labelArrival(item.getArrivalOption());
        holder.tvSubtitle.setText(recommended + " • " + arrival);
        holder.itemView.setOnClickListener(v -> listener.onHistoryClicked(item));
        holder.btnRemove.setVisibility(View.VISIBLE);
        holder.btnRemove.setOnClickListener(v -> listener.onHistoryDeleted(item));
    }

    private String labelArrival(String arrivalOption) {
        if ("plus_15".equals(arrivalOption)) return "+15 min";
        if ("plus_60".equals(arrivalOption)) return "+60 min";
        return "+30 min";
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQuery;
        private final TextView tvSubtitle;
        private final ImageButton btnRemove;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuery = itemView.findViewById(R.id.tvQuery);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }

    public interface Listener {
        void onHistoryClicked(HistoryItem item);
        void onHistoryDeleted(HistoryItem item);
    }
}
