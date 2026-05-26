package com.tripexpense.tracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.model.Expense;
import com.tripexpense.tracker.util.DateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private final OnExpenseClickListener clickListener;

    public interface OnExpenseClickListener {
        void onExpenseLongClick(Expense expense);
    }

    public ExpenseAdapter(OnExpenseClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense, clickListener);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIcon;
        private final TextView tvTitle;
        private final TextView tvPaidBy;
        private final TextView tvDate;
        private final TextView tvAmount;
        private final TextView tvSplitInfo;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_expense_category_icon);
            tvTitle = itemView.findViewById(R.id.tv_expense_title);
            tvPaidBy = itemView.findViewById(R.id.tv_expense_paid_by);
            tvDate = itemView.findViewById(R.id.tv_expense_date);
            tvAmount = itemView.findViewById(R.id.tv_expense_amount);
            tvSplitInfo = itemView.findViewById(R.id.tv_expense_split_info);
        }

        public void bind(Expense expense, OnExpenseClickListener clickListener) {
            tvTitle.setText(expense.getTitle());
            tvPaidBy.setText("Paid by " + expense.getPaidByName());
            tvDate.setText(DateUtil.formatDate(expense.getDate()));
            tvAmount.setText(String.format(Locale.getDefault(), "₹%,.2f", expense.getAmount()));
            tvSplitInfo.setText(expense.getSplitType().equals("EQUAL") ? "Split Equally" : "Split Customly");

            // Category emoji map
            String category = expense.getCategory();
            String emoji = "💸";
            if (category != null) {
                switch (category) {
                    case "Food": emoji = "🍕"; break;
                    case "Stay": emoji = "🏨"; break;
                    case "Transport": emoji = "🚗"; break;
                    case "Shopping": emoji = "🛍️"; break;
                    case "Activities": emoji = "🧗"; break;
                    case "Tickets": emoji = "🎫"; break;
                }
            }
            tvIcon.setText(emoji);

            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onExpenseLongClick(expense);
                    return true;
                }
                return false;
            });
        }
    }
}
