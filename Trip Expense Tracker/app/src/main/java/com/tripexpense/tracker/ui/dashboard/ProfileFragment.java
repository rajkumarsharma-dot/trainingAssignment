package com.tripexpense.tracker.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.service.FirebaseAuthService;
import com.tripexpense.tracker.service.FirebaseFirestoreService;
import com.tripexpense.tracker.ui.auth.AuthActivity;

public class ProfileFragment extends Fragment {

    private TextView tvInitials, tvName, tvEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvInitials = view.findViewById(R.id.tv_profile_initials);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }

        loadProfileInfo();

        return view;
    }

    private void loadProfileInfo() {
        FirebaseAuthService auth = FirebaseAuthService.getInstance();
        String currentUserId = auth.getCurrentUserId();

        if (currentUserId == null) return;

        FirebaseFirestoreService.getInstance().getUser(currentUserId).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                String email = doc.getString("email");

                if (name != null) tvName.setText(name);
                if (email != null) tvEmail.setText(email);

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
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load profile details.", Toast.LENGTH_SHORT).show());
    }

    private void performLogout() {
        FirebaseAuthService.getInstance().logout();
        Toast.makeText(getContext(), "Signed Out!", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(getActivity(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
