package com.tripexpense.tracker.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.tripexpense.tracker.model.Expense;
import com.tripexpense.tracker.model.Trip;
import com.tripexpense.tracker.model.User;

public class FirebaseFirestoreService {
    private static FirebaseFirestoreService instance;
    private final FirebaseFirestore db;

    private FirebaseFirestoreService() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseFirestoreService getInstance() {
        if (instance == null) {
            instance = new FirebaseFirestoreService();
        }
        return instance;
    }

    // --- User Operations ---
    public Task<Void> createUser(User user) {
        return db.collection("users").document(user.getUid()).set(user);
    }

    public Task<DocumentSnapshot> getUser(String uid) {
        return db.collection("users").document(uid).get();
    }

    public Task<QuerySnapshot> getUserByEmail(String email) {
        return db.collection("users").whereEqualTo("email", email.trim()).get();
    }

    // --- Trip Operations ---
    public Task<Void> createTrip(Trip trip) {
        DocumentReference ref = db.collection("trips").document();
        trip.setTripId(ref.getId());
        return ref.set(trip);
    }

    public Task<Void> deleteTrip(String tripId) {
        return db.collection("expenses").whereEqualTo("tripId", tripId).get()
            .continueWithTask(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.delete(db.collection("trips").document(tripId));
                    return batch.commit();
                } else {
                    return db.collection("trips").document(tripId).delete();
                }
            });
    }

    public ListenerRegistration listenToTrips(String userId, com.google.firebase.firestore.EventListener<QuerySnapshot> listener) {
        return db.collection("trips")
                .whereArrayContains("memberIds", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    public ListenerRegistration listenToTripDetails(String tripId, com.google.firebase.firestore.EventListener<DocumentSnapshot> listener) {
        return db.collection("trips").document(tripId).addSnapshotListener(listener);
    }

    // --- Member Operations ---
    public interface AddMemberCallback {
        void onSuccess();
        void onFailure(Exception e);
        void onUserNotFound();
    }

    public void addMemberToTrip(String tripId, String email, AddMemberCallback callback) {
        getUserByEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                User user = userDoc.toObject(User.class);
                if (user != null) {
                    DocumentReference tripRef = db.collection("trips").document(tripId);
                    db.runTransaction((Transaction.Function<Void>) transaction -> {
                        DocumentSnapshot tripSnap = transaction.get(tripRef);
                        Trip trip = tripSnap.toObject(Trip.class);
                        if (trip != null) {
                            if (!trip.getMemberIds().contains(user.getUid())) {
                                trip.getMemberIds().add(user.getUid());
                                trip.getMemberNames().put(user.getUid(), user.getName());
                                transaction.update(tripRef, "memberIds", trip.getMemberIds());
                                transaction.update(tripRef, "memberNames", trip.getMemberNames());
                            }
                        }
                        return null;
                    }).addOnSuccessListener(aVoid -> callback.onSuccess())
                      .addOnFailureListener(callback::onFailure);
                } else {
                    callback.onUserNotFound();
                }
            } else {
                callback.onUserNotFound();
            }
        });
    }

    // --- Expense Operations ---
    public Task<Void> addExpense(Expense expense) {
        DocumentReference expenseRef = db.collection("expenses").document();
        expense.setExpenseId(expenseRef.getId());

        DocumentReference tripRef = db.collection("trips").document(expense.getTripId());

        return db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.set(expenseRef, expense);
            transaction.update(tripRef, "totalExpenses", FieldValue.increment(expense.getAmount()));
            return null;
        });
    }

    public Task<Void> deleteExpense(Expense expense) {
        DocumentReference expenseRef = db.collection("expenses").document(expense.getExpenseId());
        DocumentReference tripRef = db.collection("trips").document(expense.getTripId());

        return db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.delete(expenseRef);
            transaction.update(tripRef, "totalExpenses", FieldValue.increment(-expense.getAmount()));
            return null;
        });
    }

    public ListenerRegistration listenToExpenses(String tripId, com.google.firebase.firestore.EventListener<QuerySnapshot> listener) {
        return db.collection("expenses")
                .whereEqualTo("tripId", tripId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }
}
