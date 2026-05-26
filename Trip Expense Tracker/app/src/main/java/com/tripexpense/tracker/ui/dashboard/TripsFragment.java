package com.tripexpense.tracker.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.adapter.TripAdapter;
import com.tripexpense.tracker.model.Trip;
import com.tripexpense.tracker.service.FirebaseAuthService;
import com.tripexpense.tracker.service.FirebaseFirestoreService;
import com.tripexpense.tracker.ui.trip.TripDetailsActivity;
import com.tripexpense.tracker.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

public class TripsFragment extends Fragment {

    private RecyclerView rvTrips;
    private TripAdapter adapter;
    private View llEmptyState;
    private CircularProgressIndicator loader;
    private ListenerRegistration tripsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips, container, false);

        rvTrips = view.findViewById(R.id.rv_trips);
        llEmptyState = view.findViewById(R.id.ll_trips_empty);
        loader = view.findViewById(R.id.trips_loader);
        FloatingActionButton fab = view.findViewById(R.id.fab_create_trip);

        rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TripAdapter(trip -> {
            Intent intent = new Intent(getActivity(), TripDetailsActivity.class);
            intent.putExtra("TRIP", trip);
            startActivity(intent);
        });
        rvTrips.setAdapter(adapter);

        if (fab != null) {
            fab.setOnClickListener(v -> showCreateTripDialog());
        }

        listenToTripsData();

        return view;
    }

    private void listenToTripsData() {
        String currentUserId = FirebaseAuthService.getInstance().getCurrentUserId();
        if (currentUserId == null) return;

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
                } else {
                    llEmptyState.setVisibility(View.GONE);
                    rvTrips.setVisibility(View.VISIBLE);
                    adapter.setTrips(trips);
                }
            }
        });
    }

    private void showCreateTripDialog() {
        if (getActivity() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_trip, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etTitle = view.findViewById(R.id.et_trip_title);
        TextInputEditText etDesc = view.findViewById(R.id.et_trip_desc);
        TextInputEditText etBudget = view.findViewById(R.id.et_trip_budget);

        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_trip);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_trip);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
                String budgetStr = etBudget.getText() != null ? etBudget.getText().toString().trim() : "";

                if (ValidationUtil.isEmpty(title)) {
                    etTitle.setError("Title is required");
                    return;
                }

                double budget = 0.0;
                if (!ValidationUtil.isEmpty(budgetStr)) {
                    try {
                        budget = Double.parseDouble(budgetStr);
                        if (budget < 0) {
                            etBudget.setError("Budget cannot be negative");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        etBudget.setError("Invalid number format");
                        return;
                    }
                }

                dialog.dismiss();
                saveTripToFirestore(title, desc, budget);
            });
        }
    }

    private void saveTripToFirestore(String title, String desc, double budget) {
        String currentUserId = FirebaseAuthService.getInstance().getCurrentUserId();
        if (currentUserId == null) return;

        Toast.makeText(getContext(), "Planning trip...", Toast.LENGTH_SHORT).show();

        FirebaseFirestoreService.getInstance().getUser(currentUserId).addOnSuccessListener(doc -> {
            String currentUserName = "Creator";
            if (doc.exists() && doc.get("name") != null) {
                currentUserName = doc.getString("name");
            }

            Trip trip = new Trip("", title, desc, budget, currentUserId, System.currentTimeMillis());
            trip.getMemberIds().add(currentUserId);
            trip.getMemberNames().put(currentUserId, currentUserName);

            FirebaseFirestoreService.getInstance().createTrip(trip)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Trip Planned Successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown firestore failure.";
                            Toast.makeText(getContext(), "Failed to plan trip: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tripsListener != null) {
            tripsListener.remove();
        }
    }
}
