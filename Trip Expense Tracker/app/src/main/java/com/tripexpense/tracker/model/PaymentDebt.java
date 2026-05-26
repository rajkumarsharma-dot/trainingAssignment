package com.tripexpense.tracker.model;

import java.io.Serializable;

public class PaymentDebt implements Serializable {
    private String debtorId;
    private String debtorName;
    private String creditorId;
    private String creditorName;
    private double amount;

    public PaymentDebt() {}

    public PaymentDebt(String debtorId, String debtorName, String creditorId, String creditorName, double amount) {
        this.debtorId = debtorId;
        this.debtorName = debtorName;
        this.creditorId = creditorId;
        this.creditorName = creditorName;
        this.amount = amount;
    }

    public String getDebtorId() { return debtorId; }
    public void setDebtorId(String debtorId) { this.debtorId = debtorId; }

    public String getDebtorName() { return debtorName; }
    public void setDebtorName(String debtorName) { this.debtorName = debtorName; }

    public String getCreditorId() { return creditorId; }
    public void setCreditorId(String creditorId) { this.creditorId = creditorId; }

    public String getCreditorName() { return creditorName; }
    public void setCreditorName(String creditorName) { this.creditorName = creditorName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
