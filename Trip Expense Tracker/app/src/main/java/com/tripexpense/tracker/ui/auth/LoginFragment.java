package com.tripexpense.tracker.ui.auth;

import android.content.Intent;
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
import com.tripexpense.tracker.ui.dashboard.MainActivity;
import com.tripexpense.tracker.util.ValidationUtil;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etEmail = view.findViewById(R.id.et_login_email);
        etPassword = view.findViewById(R.id.et_login_password);
        tilEmail = view.findViewById(R.id.til_login_email);
        tilPassword = view.findViewById(R.id.til_login_password);

        MaterialButton btnLogin = view.findViewById(R.id.btn_login);
        View tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
        View tvGoToRegister = view.findViewById(R.id.tv_go_to_register);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).loadFragment(new ForgotPasswordFragment(), true);
                }
            });
        }

        if (tvGoToRegister != null) {
            tvGoToRegister.setOnClickListener(v -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).loadFragment(new RegisterFragment(), true);
                }
            });
        }

        return view;
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean isValid = true;

        if (!ValidationUtil.isValidEmail(email)) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (ValidationUtil.isEmpty(password)) {
            tilPassword.setError("Password cannot be empty");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!isValid) return;

        Toast.makeText(getContext(), "Signing in...", Toast.LENGTH_SHORT).show();

        FirebaseAuthService.getInstance().login(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    } else {
                        String errMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                        Toast.makeText(getContext(), "Error: " + errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
