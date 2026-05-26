package com.tripexpense.tracker.model;

public class Balance {
    private String memberId;
    private String memberName;
    private double totalPaid;
    private double totalShare;
    private double netBalance; // totalPaid - totalShare

    public Balance() {}

    public Balance(String memberId, String memberName, double totalPaid, double totalShare) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.totalPaid = totalPaid;
        this.totalShare = totalShare;
        this.netBalance = totalPaid - totalShare;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { 
        this.totalPaid = totalPaid; 
        this.netBalance = this.totalPaid - this.totalShare;
    }

    public double getTotalShare() { return totalShare; }
    public void setTotalShare(double totalShare) { 
        this.totalShare = totalShare; 
        this.netBalance = this.totalPaid - this.totalShare;
    }

    public double getNetBalance() { return netBalance; }
}
