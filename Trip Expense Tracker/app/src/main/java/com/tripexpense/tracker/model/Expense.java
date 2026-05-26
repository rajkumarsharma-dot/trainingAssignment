package com.tripexpense.tracker.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Expense implements Serializable {
    private String expenseId;
    private String tripId;
    private String title;
    private double amount;
    private String paidById;
    private String paidByName;
    private String category;
    private long date;
    private String notes;
    private String splitType; // "EQUAL" or "CUSTOM"
    private Map<String, Double> splits = new HashMap<>(); // memberId -> split amount

    // Required for Firebase
    public Expense() {}

    public Expense(String expenseId, String tripId, String title, double amount, String paidById, String paidByName, String category, long date, String notes, String splitType, Map<String, Double> splits) {
        this.expenseId = expenseId;
        this.tripId = tripId;
        this.title = title;
        this.amount = amount;
        this.paidById = paidById;
        this.paidByName = paidByName;
        this.category = category;
        this.date = date;
        this.notes = notes;
        this.splitType = splitType;
        this.splits = splits;
    }

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaidById() { return paidById; }
    public void setPaidById(String paidById) { this.paidById = paidById; }

    public String getPaidByName() { return paidByName; }
    public void setPaidByName(String paidByName) { this.paidByName = paidByName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }

    public Map<String, Double> getSplits() { return splits; }
    public void setSplits(Map<String, Double> splits) { this.splits = splits; }
}
