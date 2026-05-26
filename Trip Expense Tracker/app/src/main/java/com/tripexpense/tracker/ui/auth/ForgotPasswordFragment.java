package com.tripexpense.tracker.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.service.FirebaseAuthService;
import com.tripexpense.tracker.util.ValidationUtil;

public class ForgotPasswordFragment extends Fragment {

    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        etEmail = view.findViewById(R.id.et_reset_email);
        tilEmail = view.findViewById(R.id.til_reset_email);

        MaterialButton btnReset = view.findViewById(R.id.btn_reset_password);
        View tvBackToLogin = view.findViewById(R.id.tv_back_to_login);

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> performReset());
        }

        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).loadFragment(new LoginFragment(), false);
                }
            });
        }

        return view;
    }

    private void performReset() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (!ValidationUtil.isValidEmail(email)) {
            tilEmail.setError("Enter a valid email address");
            return;
        } else {
            tilEmail.setError(null);
        }

        Toast.makeText(getContext(), "Sending recovery email...", Toast.LENGTH_SHORT).show();

        FirebaseAuthService.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Recovery Link Sent! Check your Inbox.", Toast.LENGTH_LONG).show();
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).loadFragment(new LoginFragment(), false);
                        }
                    } else {
                        String errMsg = task.getException() != null ? task.getException().getMessage() : "Failed to trigger reset email.";
                        Toast.makeText(getContext(), "Error: " + errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
