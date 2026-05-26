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
import com.tripexpense.tracker.model.User;
import com.tripexpense.tracker.service.FirebaseAuthService;
import com.tripexpense.tracker.service.FirebaseFirestoreService;
import com.tripexpense.tracker.ui.dashboard.MainActivity;
import com.tripexpense.tracker.util.ValidationUtil;

public class RegisterFragment extends Fragment {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        etName = view.findViewById(R.id.et_register_name);
        etEmail = view.findViewById(R.id.et_register_email);
        etPassword = view.findViewById(R.id.et_register_password);
        etConfirmPassword = view.findViewById(R.id.et_register_confirm_password);

        tilName = view.findViewById(R.id.til_register_name);
        tilEmail = view.findViewById(R.id.til_register_email);
        tilPassword = view.findViewById(R.id.til_register_password);
        tilConfirmPassword = view.findViewById(R.id.til_register_confirm_password);

        MaterialButton btnRegister = view.findViewById(R.id.btn_register);
        View tvGoToLogin = view.findViewById(R.id.tv_go_to_login);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> performRegistration());
        }

        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).loadFragment(new LoginFragment(), false);
                }
            });
        }

        return view;
    }

    private void performRegistration() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        boolean isValid = true;

        if (ValidationUtil.isEmpty(name)) {
            tilName.setError("Name is required");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        if (!ValidationUtil.isValidEmail(email)) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (!ValidationUtil.isValidPassword(password)) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (!isValid) return;

        Toast.makeText(getContext(), "Creating Account...", Toast.LENGTH_SHORT).show();

        FirebaseAuthService.getInstance().signup(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        User newUser = new User(uid, name, email, "", System.currentTimeMillis());

                        FirebaseFirestoreService.getInstance().createUser(newUser)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(getContext(), "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), MainActivity.class));
                                        if (getActivity() != null) {
                                            getActivity().finish();
                                        }
                                    } else {
                                        String errMsg = dbTask.getException() != null ? dbTask.getException().getMessage() : "Failed to record user database entry.";
                                        Toast.makeText(getContext(), "Firestore Error: " + errMsg, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        String errMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                        Toast.makeText(getContext(), "Error: " + errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
