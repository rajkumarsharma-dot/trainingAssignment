package com.tripexpense.tracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.model.Trip;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> trips = new ArrayList<>();
    private final OnTripClickListener listener;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public TripAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips != null ? trips : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.bind(trip, listener);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvStatus;
        private final TextView tvDescription;
        private final TextView tvMembersCount;
        private final TextView tvTotalExpenses;
        private final TextView tvBudget;
        private final LinearProgressIndicator pbBudget;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_trip_title);
            tvStatus = itemView.findViewById(R.id.tv_trip_status);
            tvDescription = itemView.findViewById(R.id.tv_trip_description);
            tvMembersCount = itemView.findViewById(R.id.tv_trip_members_count);
            tvTotalExpenses = itemView.findViewById(R.id.tv_trip_total_expenses);
            tvBudget = itemView.findViewById(R.id.tv_trip_budget);
            pbBudget = itemView.findViewById(R.id.pb_trip_budget);
        }

        public void bind(Trip trip, OnTripClickListener listener) {
            tvTitle.setText(trip.getTitle());
            tvDescription.setText(trip.getDescription());
            
            int members = trip.getMemberIds() != null ? trip.getMemberIds().size() : 0;
            tvMembersCount.setText(String.format(Locale.getDefault(), "%d Members", members));
            
            double total = trip.getTotalExpenses();
            double budget = trip.getBudget();
            
            tvTotalExpenses.setText(String.format(Locale.getDefault(), "₹%,.2f", total));
            tvBudget.setText(String.format(Locale.getDefault(), "Budget: ₹%,.0f", budget));

            // Status Badge
            if (budget > 0 && total >= budget) {
                tvStatus.setText("Over Budget");
                tvStatus.setBackgroundResource(R.drawable.bg_badge_settled);
            } else {
                tvStatus.setText("Active");
                tvStatus.setBackgroundResource(R.drawable.bg_badge_active);
            }

            // Progress Bar
            if (budget > 0) {
                int progress = (int) ((total / budget) * 100);
                pbBudget.setProgress(Math.min(progress, 100));
            } else {
                pbBudget.setProgress(0);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripClick(trip);
                }
            });
        }
    }
}
