package com.tripexpense.tracker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trip implements Serializable {
    private String tripId;
    private String title;
    private String description;
    private double budget;
    private String creatorId;
    private long createdAt;
    private List<String> memberIds = new ArrayList<>();
    private Map<String, String> memberNames = new HashMap<>(); // userId -> name
    private double totalExpenses;

    // Required for Firebase
    public Trip() {}

    public Trip(String tripId, String title, String description, double budget, String creatorId, long createdAt) {
        this.tripId = tripId;
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.totalExpenses = 0.0;
    }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public Map<String, String> getMemberNames() { return memberNames; }
    public void setMemberNames(Map<String, String> memberNames) { this.memberNames = memberNames; }

    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }
}
