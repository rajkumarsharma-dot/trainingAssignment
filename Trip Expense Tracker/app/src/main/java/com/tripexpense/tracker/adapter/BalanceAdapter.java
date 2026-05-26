package com.tripexpense.tracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.model.Balance;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder> {

    private List<Balance> balances = new ArrayList<>();

    public void setBalances(List<Balance> balances) {
        this.balances = balances != null ? balances : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_balance, parent, false);
        return new BalanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
        Balance balance = balances.get(position);
        holder.bind(balance);
    }

    @Override
    public int getItemCount() {
        return balances.size();
    }

    static class BalanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInitials;
        private final TextView tvName;
        private final TextView tvDetails;
        private final TextView tvStatus;

        public BalanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_balance_initials);
            tvName = itemView.findViewById(R.id.tv_balance_name);
            tvDetails = itemView.findViewById(R.id.tv_balance_details);
            tvStatus = itemView.findViewById(R.id.tv_balance_status);
        }

        public void bind(Balance balance) {
            String name = balance.getMemberName();
            tvName.setText(name);

            // Extract Initials
            String initials = "U";
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.trim().split("\\s+");
                if (parts.length > 0) {
                    String first = parts[0].substring(0, 1).toUpperCase();
                    String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1).toUpperCase() : "";
                    initials = first + last;
                }
            }
            tvInitials.setText(initials);

            tvDetails.setText(String.format(Locale.getDefault(), "Paid: ₹%,.2f | Share: ₹%,.2f", balance.getTotalPaid(), balance.getTotalShare()));

            double net = balance.getNetBalance();
            if (net > 0.01) {
                tvStatus.setText(String.format(Locale.getDefault(), "+₹%,.2f", net));
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success));
            } else if (net < -0.01) {
                tvStatus.setText(String.format(Locale.getDefault(), "-₹%,.2f", Math.abs(net)));
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error));
            } else {
                tvStatus.setText("Settled");
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary_light));
            }
        }
    }
}
