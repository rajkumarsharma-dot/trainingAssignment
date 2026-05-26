package com.tripexpense.tracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tripexpense.tracker.R;
import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<String> memberNames = new ArrayList<>();

    public void setMembers(List<String> memberNames) {
        this.memberNames = memberNames != null ? memberNames : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String name = memberNames.get(position);
        holder.bind(name);
    }

    @Override
    public int getItemCount() {
        return memberNames.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInitials;
        private final TextView tvName;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_member_initials);
            tvName = itemView.findViewById(R.id.tv_member_name);
        }

        public void bind(String name) {
            tvName.setText(name);
            
            // Extract initials
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
        }
    }
}
