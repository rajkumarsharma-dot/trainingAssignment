package com.tripexpense.tracker.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tripexpense.tracker.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.SplitViewHolder> {

    private List<String> memberIds = new ArrayList<>();
    private Map<String, String> memberNames = new HashMap<>();
    private final Map<String, Double> customSplits = new HashMap<>();
    private final OnSplitChangedListener changeListener;

    public interface OnSplitChangedListener {
        void onSplitChanged(Map<String, Double> splits);
    }

    public SplitAdapter(OnSplitChangedListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setMembers(List<String> ids, Map<String, String> names) {
        this.memberIds = ids != null ? ids : new ArrayList<>();
        this.memberNames = names != null ? names : new HashMap<>();
        customSplits.clear();
        for (String mId : memberIds) {
            customSplits.put(mId, 0.0);
        }
        notifyDataSetChanged();
    }

    public Map<String, Double> getCustomSplits() {
        return customSplits;
    }

    @NonNull
    @Override
    public SplitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_split, parent, false);
        return new SplitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitViewHolder holder, int position) {
        String mId = memberIds.get(position);
        String name = memberNames.get(mId);
        if (name == null) name = mId;

        holder.bind(mId, name, customSplits.get(mId), (memberId, val) -> {
            customSplits.put(memberId, val);
            if (changeListener != null) {
                changeListener.onSplitChanged(customSplits);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberIds.size();
    }

    static class SplitViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInitials;
        private final TextView tvName;
        private final EditText etAmount;

        private interface OnValueBindCallback {
            void onValueChanged(String memberId, double val);
        }

        public SplitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_split_member_initials);
            tvName = itemView.findViewById(R.id.tv_split_member_name);
            etAmount = itemView.findViewById(R.id.et_split_amount);
        }

        public void bind(String memberId, String name, Double currentVal, OnValueBindCallback callback) {
            tvName.setText(name);

            // Set Initials
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

            // Temporarily clear focus changes
            etAmount.setOnFocusChangeListener(null);
            
            if (currentVal == null || currentVal == 0.0) {
                etAmount.setText("");
            } else {
                etAmount.setText(String.valueOf(currentVal));
            }

            etAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    double val = 0.0;
                    if (s != null && s.length() > 0) {
                        try {
                            val = Double.parseDouble(s.toString());
                        } catch (NumberFormatException ignored) {}
                    }
                    callback.onValueChanged(memberId, val);
                }
            });
        }
    }
}
