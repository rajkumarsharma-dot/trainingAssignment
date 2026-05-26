package com.tripexpense.tracker.ui.trip;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.adapter.BalanceAdapter;
import com.tripexpense.tracker.adapter.ExpenseAdapter;
import com.tripexpense.tracker.model.Balance;
import com.tripexpense.tracker.model.Expense;
import com.tripexpense.tracker.model.PaymentDebt;
import com.tripexpense.tracker.model.Trip;
import com.tripexpense.tracker.service.FirebaseAuthService;
import com.tripexpense.tracker.service.FirebaseFirestoreService;
import com.tripexpense.tracker.ui.analytics.AnalyticsActivity;
import com.tripexpense.tracker.ui.expense.ExpenseActivity;
import com.tripexpense.tracker.util.CalculationUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TripDetailsActivity extends AppCompatActivity {

    private Trip trip;
    private RecyclerView rvExpenses, rvBalances;
    private ExpenseAdapter expenseAdapter;
    private BalanceAdapter balanceAdapter;
    private TextView tvTitle, tvDesc, tvTotal, tvBudget, tvSettlementsLedger;
    private LinearProgressIndicator pbBudget;
    private View flExpensesContainer, flBalancesContainer, llBalancesActions, llExpensesEmpty;
    private View detailsLoader;
    private MaterialButton btnTabExpenses, btnTabBalances;
    private FloatingActionButton fabAddExpense;

    private ListenerRegistration tripListener;
    private ListenerRegistration expensesListener;

    private List<Expense> activeExpensesList = new ArrayList<>();
    private List<Balance> activeBalancesList = new ArrayList<>();
    private List<PaymentDebt> activeSettlementsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        trip = (Trip) getIntent().getSerializableExtra("TRIP");
        if (trip == null) {
            Toast.makeText(this, "Failed to load trip context.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind Views
        tvTitle = findViewById(R.id.tv_details_title);
        tvDesc = findViewById(R.id.tv_details_desc);
        tvTotal = findViewById(R.id.tv_details_total);
        tvBudget = findViewById(R.id.tv_details_budget);
        tvSettlementsLedger = findViewById(R.id.tv_settlements_ledger);
        pbBudget = findViewById(R.id.pb_details_budget);

        flExpensesContainer = findViewById(R.id.fl_expenses_container);
        flBalancesContainer = findViewById(R.id.fl_balances_container);
        llBalancesActions = findViewById(R.id.ll_balances_actions);
        llExpensesEmpty = findViewById(R.id.ll_expenses_empty);
        detailsLoader = findViewById(R.id.details_loader);

        btnTabExpenses = findViewById(R.id.btn_tab_expenses);
        btnTabBalances = findViewById(R.id.btn_tab_balances);
        fabAddExpense = findViewById(R.id.fab_add_expense);

        rvExpenses = findViewById(R.id.rv_details_expenses);
        rvBalances = findViewById(R.id.rv_details_balances);

        // Configure lists
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(this::showDeleteExpenseConfirmation);
        rvExpenses.setAdapter(expenseAdapter);

        rvBalances.setLayoutManager(new LinearLayoutManager(this));
        balanceAdapter = new BalanceAdapter();
        rvBalances.setAdapter(balanceAdapter);

        // Bind Actions
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_analytics).setOnClickListener(v -> openAnalytics());
        btnTabExpenses.setOnClickListener(v -> switchTab(true));
        btnTabBalances.setOnClickListener(v -> switchTab(false));
        
        if (fabAddExpense != null) {
            fabAddExpense.setOnClickListener(v -> {
                Intent intent = new Intent(TripDetailsActivity.this, ExpenseActivity.class);
                intent.putExtra("TRIP", trip);
                startActivity(intent);
            });
        }

        findViewById(R.id.btn_add_member).setOnClickListener(v -> showAddMemberDialog());
        findViewById(R.id.btn_settle_up).setOnClickListener(v -> showSettleUpDialog());

        listenToTripData();
    }

    private void listenToTripData() {
        tripListener = FirebaseFirestoreService.getInstance().listenToTripDetails(trip.getTripId(), (snapshot, e) -> {
            if (e != null) {
                Toast.makeText(this, "Failed sync: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Trip updated = snapshot.toObject(Trip.class);
                if (updated != null) {
                    trip = updated;
                    updateTripUIElements();
                }
            }
        });

        expensesListener = FirebaseFirestoreService.getInstance().listenToExpenses(trip.getTripId(), (snapshot, e) -> {
            if (detailsLoader != null) detailsLoader.setVisibility(View.GONE);
            if (e != null) {
                Toast.makeText(this, "Failed sync: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null) {
                activeExpensesList.clear();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Expense exp = doc.toObject(Expense.class);
                    if (exp != null) {
                        activeExpensesList.add(exp);
                    }
                }

                if (activeExpensesList.isEmpty()) {
                    llExpensesEmpty.setVisibility(View.VISIBLE);
                    rvExpenses.setVisibility(View.GONE);
                } else {
                    llExpensesEmpty.setVisibility(View.GONE);
                    rvExpenses.setVisibility(View.VISIBLE);
                    expenseAdapter.setExpenses(activeExpensesList);
                }

                recalculateBalancesAndSettlements();
            }
        });
    }

    private void updateTripUIElements() {
        tvTitle.setText(trip.getTitle());
        tvDesc.setText(trip.getDescription());
        
        double total = trip.getTotalExpenses();
        double budget = trip.getBudget();

        tvTotal.setText(String.format(Locale.getDefault(), "₹%,.2f", total));
        tvBudget.setText(String.format(Locale.getDefault(), "Budget: ₹%,.0f", budget));

        if (budget > 0) {
            int progress = (int) ((total / budget) * 100);
            pbBudget.setProgress(Math.min(progress, 100));
            if (progress >= 100) {
                pbBudget.setIndicatorColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error)));
            } else {
                pbBudget.setIndicatorColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent)));
            }
        } else {
            pbBudget.setProgress(0);
        }
    }

    private void recalculateBalancesAndSettlements() {
        activeBalancesList = CalculationUtil.calculateBalances(trip.getMemberIds(), trip.getMemberNames(), activeExpensesList);
        balanceAdapter.setBalances(activeBalancesList);

        activeSettlementsList = CalculationUtil.settleUp(activeBalancesList);
        
        if (activeSettlementsList.isEmpty()) {
            tvSettlementsLedger.setText("Everyone is settled up!");
            tvSettlementsLedger.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark));
        } else {
            StringBuilder ledger = new StringBuilder();
            for (PaymentDebt debt : activeSettlementsList) {
                ledger.append(String.format(Locale.getDefault(), "• %s owes %s ₹%,.2f\n", 
                        debt.getDebtorName(), debt.getCreditorName(), debt.getAmount()));
            }
            tvSettlementsLedger.setText(ledger.toString().trim());
            tvSettlementsLedger.setTextColor(ContextCompat.getColor(this, R.color.text_primary_light));
        }
    }

    private void switchTab(boolean showExpenses) {
        if (showExpenses) {
            flExpensesContainer.setVisibility(View.VISIBLE);
            fabAddExpense.setVisibility(View.VISIBLE);
            flBalancesContainer.setVisibility(View.GONE);
            llBalancesActions.setVisibility(View.GONE);

            btnTabExpenses.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light)));
            btnTabExpenses.setTextColor(ContextCompat.getColor(this, R.color.primary));

            btnTabBalances.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent)));
            btnTabBalances.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark));
        } else {
            flExpensesContainer.setVisibility(View.GONE);
            fabAddExpense.setVisibility(View.GONE);
            flBalancesContainer.setVisibility(View.VISIBLE);
            llBalancesActions.setVisibility(View.VISIBLE);

            btnTabBalances.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light)));
            btnTabBalances.setTextColor(ContextCompat.getColor(this, R.color.primary));

            btnTabExpenses.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent)));
            btnTabExpenses.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark));
        }
    }

    private void showDeleteExpenseConfirmation(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete '" + expense.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Toast.makeText(this, "Deleting expense...", Toast.LENGTH_SHORT).show();
                    FirebaseFirestoreService.getInstance().deleteExpense(expense)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Error deleting: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Friend by Email");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_trip, null);
        builder.setView(view);

        view.findViewById(R.id.tv_splash_title).setVisibility(View.GONE);
        EditText etEmail = view.findViewById(R.id.et_trip_title);
        etEmail.setHint("Friend's Email Address");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        view.findViewById(R.id.til_trip_desc).setVisibility(View.GONE);
        view.findViewById(R.id.til_trip_budget).setVisibility(View.GONE);

        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_trip);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_trip);
        btnSave.setText("Add");

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                return;
            }
            dialog.dismiss();
            Toast.makeText(this, "Inviting member...", Toast.LENGTH_SHORT).show();

            FirebaseFirestoreService.getInstance().addMemberToTrip(trip.getTripId(), email, new FirebaseFirestoreService.AddMemberCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(TripDetailsActivity.this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(TripDetailsActivity.this, "Transaction failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onUserNotFound() {
                    Toast.makeText(TripDetailsActivity.this, "User not registered in SplitTrip.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void showSettleUpDialog() {
        if (activeSettlementsList.isEmpty()) {
            Toast.makeText(this, "All balances are settled!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] settlementStrings = new String[activeSettlementsList.size()];
        for (int i = 0; i < activeSettlementsList.size(); i++) {
            PaymentDebt debt = activeSettlementsList.get(i);
            settlementStrings[i] = String.format(Locale.getDefault(), "%s pay %s: ₹%,.2f", 
                    debt.getDebtorName(), debt.getCreditorName(), debt.getAmount());
        }

        new AlertDialog.Builder(this)
                .setTitle("Select a Debt to Settle")
                .setItems(settlementStrings, (dialog, which) -> {
                    PaymentDebt selected = activeSettlementsList.get(which);
                    confirmSettlementPayment(selected);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmSettlementPayment(PaymentDebt debt) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Settlement")
                .setMessage(String.format(Locale.getDefault(), "Did %s pay %s ₹%,.2f?", 
                        debt.getDebtorName(), debt.getCreditorName(), debt.getAmount()))
                .setPositiveButton("Mark as Settled", (dialog, which) -> {
                    Toast.makeText(this, "Recording settlement...", Toast.LENGTH_SHORT).show();

                    Map<String, Double> splits = new HashMap<>();
                    splits.put(debt.getCreditorId(), debt.getAmount());
                    splits.put(debt.getDebtorId(), 0.0);

                    Expense settleExp = new Expense(
                            "", trip.getTripId(),
                            String.format("Settle: %s → %s", debt.getDebtorName(), debt.getCreditorName()),
                            debt.getAmount(),
                            debt.getDebtorId(), debt.getDebtorName(),
                            "Tickets", System.currentTimeMillis(),
                            "Settlement transfer", "CUSTOM", splits
                    );

                    FirebaseFirestoreService.getInstance().addExpense(settleExp)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Settlement recorded!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed settlement: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAnalytics() {
        Intent intent = new Intent(this, AnalyticsActivity.class);
        intent.putExtra("TRIP", trip);
        intent.putExtra("EXPENSES", (Serializable) activeExpensesList);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tripListener != null) tripListener.remove();
        if (expensesListener != null) expensesListener.remove();
    }
}
