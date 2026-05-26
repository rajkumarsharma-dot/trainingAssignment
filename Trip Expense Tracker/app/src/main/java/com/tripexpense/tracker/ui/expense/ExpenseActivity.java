package com.tripexpense.tracker.ui.expense;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.adapter.SplitAdapter;
import com.tripexpense.tracker.model.Expense;
import com.tripexpense.tracker.model.Trip;
import com.tripexpense.tracker.service.FirebaseFirestoreService;
import com.tripexpense.tracker.util.CalculationUtil;
import com.tripexpense.tracker.util.ValidationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseActivity extends AppCompatActivity {

    private Trip trip;
    private TextInputEditText etTitle, etAmount, etNotes;
    private TextInputLayout tilTitle, tilAmount;
    private Spinner spinnerCategory, spinnerPayer;
    private MaterialButton btnEqual, btnCustom, btnSave;
    private TextView tvEqualSummary, tvSplitSumStatus;
    private View llCustomContainer;
    private RecyclerView rvSplitMembers;
    private SplitAdapter splitAdapter;

    private boolean isSplitEqual = true;
    private double currentAmount = 0.0;
    private final List<String> payerIds = new ArrayList<>();
    private final List<String> payerNames = new ArrayList<>();
    private final String[] categories = {"Food", "Stay", "Transport", "Shopping", "Activities", "Tickets", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        trip = (Trip) getIntent().getSerializableExtra("TRIP");
        if (trip == null) {
            Toast.makeText(this, "Failed to load trip context.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle = findViewById(R.id.et_exp_title);
        etAmount = findViewById(R.id.et_exp_amount);
        etNotes = findViewById(R.id.et_exp_notes);

        tilTitle = findViewById(R.id.til_exp_title);
        tilAmount = findViewById(R.id.til_exp_amount);

        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerPayer = findViewById(R.id.spinner_payer);

        btnEqual = findViewById(R.id.btn_split_equal);
        btnCustom = findViewById(R.id.btn_split_custom);
        btnSave = findViewById(R.id.btn_save_expense);

        tvEqualSummary = findViewById(R.id.tv_equal_share_summary);
        tvSplitSumStatus = findViewById(R.id.tv_split_sum_status);
        llCustomContainer = findViewById(R.id.ll_custom_split_container);
        rvSplitMembers = findViewById(R.id.rv_split_members);

        findViewById(R.id.btn_expense_back).setOnClickListener(v -> finish());

        setupSpinners();
        setupSplitEngine();

        btnEqual.setOnClickListener(v -> toggleSplitMode(true));
        btnCustom.setOnClickListener(v -> toggleSplitMode(false));
        btnSave.setOnClickListener(v -> saveExpense());

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
                currentAmount = val;
                updateSplitView();
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        payerIds.clear();
        payerNames.clear();
        for (Map.Entry<String, String> entry : trip.getMemberNames().entrySet()) {
            payerIds.add(entry.getKey());
            payerNames.add(entry.getValue());
        }

        ArrayAdapter<String> payAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, payerNames);
        payAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayer.setAdapter(payAdapter);
    }

    private void setupSplitEngine() {
        rvSplitMembers.setLayoutManager(new LinearLayoutManager(this));
        splitAdapter = new SplitAdapter(splits -> validateCustomSplits(splits));
        rvSplitMembers.setAdapter(splitAdapter);
        splitAdapter.setMembers(trip.getMemberIds(), trip.getMemberNames());
    }

    private void toggleSplitMode(boolean equal) {
        isSplitEqual = equal;
        if (equal) {
            btnEqual.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light)));
            btnEqual.setTextColor(ContextCompat.getColor(this, R.color.primary));

            btnCustom.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent)));
            btnCustom.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark));

            tvEqualSummary.setVisibility(View.VISIBLE);
            llCustomContainer.setVisibility(View.GONE);
        } else {
            btnCustom.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light)));
            btnCustom.setTextColor(ContextCompat.getColor(this, R.color.primary));

            btnEqual.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent)));
            btnEqual.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark));

            tvEqualSummary.setVisibility(View.GONE);
            llCustomContainer.setVisibility(View.VISIBLE);
        }
        updateSplitView();
    }

    private void updateSplitView() {
        if (isSplitEqual) {
            double share = 0.0;
            int members = trip.getMemberIds().size();
            if (members > 0 && currentAmount > 0) {
                share = CalculationUtil.round(currentAmount / members);
            }
            tvEqualSummary.setText(String.format(Locale.getDefault(), "Each member's share: ₹%,.2f", share));
            btnSave.setEnabled(true);
        } else {
            validateCustomSplits(splitAdapter.getCustomSplits());
        }
    }

    private void validateCustomSplits(Map<String, Double> splits) {
        if (isSplitEqual) return;

        double sum = 0.0;
        for (double val : splits.values()) {
            sum += val;
        }
        sum = CalculationUtil.round(sum);

        double remainder = CalculationUtil.round(currentAmount - sum);

        tvSplitSumStatus.setText(String.format(Locale.getDefault(), "Sum: ₹%,.2f | Remaining: ₹%,.2f", sum, remainder));

        if (currentAmount > 0 && Math.abs(remainder) <= 0.01) {
            tvSplitSumStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
            btnSave.setEnabled(true);
        } else {
            tvSplitSumStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
            btnSave.setEnabled(false);
        }
    }

    private void saveExpense() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        boolean isValid = true;

        if (ValidationUtil.isEmpty(title)) {
            tilTitle.setError("Title is required");
            isValid = false;
        } else {
            tilTitle.setError(null);
        }

        if (!ValidationUtil.isValidAmount(amountStr)) {
            tilAmount.setError("Enter a valid positive amount");
            isValid = false;
        } else {
            tilAmount.setError(null);
        }

        if (!isValid) return;

        double amount = Double.parseDouble(amountStr);
        String category = categories[spinnerCategory.getSelectedItemPosition()];
        
        int payerIndex = spinnerPayer.getSelectedItemPosition();
        String paidById = payerIds.get(payerIndex);
        String paidByName = payerNames.get(payerIndex);

        Map<String, Double> splits;
        if (isSplitEqual) {
            splits = CalculationUtil.splitEqually(amount, trip.getMemberIds());
        } else {
            splits = new HashMap<>(splitAdapter.getCustomSplits());
        }

        Toast.makeText(this, "Saving expense...", Toast.LENGTH_SHORT).show();

        Expense expense = new Expense(
                "", trip.getTripId(), title, amount,
                paidById, paidByName, category,
                System.currentTimeMillis(), notes,
                isSplitEqual ? "EQUAL" : "CUSTOM", splits
        );

        FirebaseFirestoreService.getInstance().addExpense(expense)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ExpenseActivity.this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(ExpenseActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
