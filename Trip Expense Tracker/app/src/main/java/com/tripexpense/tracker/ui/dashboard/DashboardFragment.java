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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.adapter.TripAdapter;
import com.tripexpense.tracker.model.Balance;
import com.tripexpense.tracker.model.Expense;
import com.tripexpense.tracker.model.Trip;
import com.tripexpense.tracker.service.FirebaseAuthService;
import com.tripexpense.tracker.service.FirebaseFirestoreService;
import com.tripexpense.tracker.ui.trip.TripDetailsActivity;
import com.tripexpense.tracker.util.CalculationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private RecyclerView rvTrips;
    private TripAdapter adapter;
    private TextView tvWelcome, tvOwed, tvOwes;
    private View llEmptyState;
    private CircularProgressIndicator loader;
    private ListenerRegistration tripsListener;
    private final List<ListenerRegistration> expenseListeners = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvWelcome = view.findViewById(R.id.tv_dash_welcome);
        tvOwed = view.findViewById(R.id.tv_dash_owed);
        tvOwes = view.findViewById(R.id.tv_dash_owes);
        rvTrips = view.findViewById(R.id.rv_dash_trips);
        llEmptyState = view.findViewById(R.id.ll_dash_empty);
        loader = view.findViewById(R.id.dash_loader);

        rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TripAdapter(trip -> {
            Intent intent = new Intent(getActivity(), TripDetailsActivity.class);
            intent.putExtra("TRIP", trip);
            startActivity(intent);
        });
        rvTrips.setAdapter(adapter);

        loadDashboardData();

        return view;
    }

    private void loadDashboardData() {
        FirebaseAuthService auth = FirebaseAuthService.getInstance();
        String currentUserId = auth.getCurrentUserId();

        if (currentUserId == null) return;

        FirebaseFirestoreService.getInstance().getUser(currentUserId).addOnSuccessListener(doc -> {
            if (doc.exists() && doc.get("name") != null) {
                tvWelcome.setText("Hello, " + doc.getString("name"));
            }
        });

        tripsListener = FirebaseFirestoreService.getInstance().listenToTrips(currentUserId, (snapshot, e) -> {
            if (loader != null) loader.setVisibility(View.GONE);
            if (e != null) {
                Toast.makeText(getContext(), "Error fetching trips: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null) {
                List<Trip> trips = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Trip trip = doc.toObject(Trip.class);
                    if (trip != null) {
                        trips.add(trip);
                    }
                }

                if (trips.isEmpty()) {
                    llEmptyState.setVisibility(View.VISIBLE);
                    rvTrips.setVisibility(View.GONE);
                    tvOwed.setText("₹0.00");
                    tvOwes.setText("₹0.00");
                } else {
                    llEmptyState.setVisibility(View.GONE);
                    rvTrips.setVisibility(View.VISIBLE);
                    adapter.setTrips(trips);
                    calculateAggregates(trips, currentUserId);
                }
            }
        });
    }

    private void calculateAggregates(List<Trip> trips, String currentUserId) {
        for (ListenerRegistration reg : expenseListeners) {
            reg.remove();
        }
        expenseListeners.clear();

        final double[] totalOwed = {0.0};
        final double[] totalOwes = {0.0};

        for (Trip trip : trips) {
            ListenerRegistration expListener = FirebaseFirestoreService.getInstance().listenToExpenses(trip.getTripId(), (snapshot, e) -> {
                if (e == null && snapshot != null) {
                    List<Expense> expenses = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Expense exp = doc.toObject(Expense.class);
                        if (exp != null) {
                            expenses.add(exp);
                        }
                    }

                    List<Balance> balances = CalculationUtil.calculateBalances(trip.getMemberIds(), trip.getMemberNames(), expenses);
                    for (Balance b : balances) {
                        if (b.getMemberId().equals(currentUserId)) {
                            double net = b.getNetBalance();
                            if (net > 0) {
                                totalOwed[0] += net;
                            } else if (net < 0) {
                                totalOwes[0] += Math.abs(net);
                            }
                        }
                    }

                    tvOwed.setText(String.format(Locale.getDefault(), "₹%,.2f", totalOwed[0]));
                    tvOwes.setText(String.format(Locale.getDefault(), "₹%,.2f", totalOwes[0]));
                }
            });
            expenseListeners.add(expListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tripsListener != null) {
            tripsListener.remove();
        }
        for (ListenerRegistration reg : expenseListeners) {
            reg.remove();
        }
    }
}
