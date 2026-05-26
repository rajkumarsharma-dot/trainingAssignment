package com.tripexpense.tracker.util;

import com.tripexpense.tracker.model.Balance;
import com.tripexpense.tracker.model.Expense;
import com.tripexpense.tracker.model.PaymentDebt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculationUtil {

    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Map<String, Double> splitEqually(double amount, List<String> memberIds) {
        Map<String, Double> result = new HashMap<>();
        if (memberIds == null || memberIds.isEmpty() || amount <= 0) {
            return result;
        }

        double rawShare = amount / memberIds.size();
        double roundedShare = round(rawShare);
        double totalRounded = roundedShare * memberIds.size();
        double difference = round(amount - totalRounded);

        for (int i = 0; i < memberIds.size(); i++) {
            String mId = memberIds.get(i);
            if (i == 0) {
                // Give difference to first member to ensure sum matches exactly
                result.put(mId, round(roundedShare + difference));
            } else {
                result.put(mId, roundedShare);
            }
        }
        return result;
    }

    public static List<Balance> calculateBalances(List<String> memberIds, Map<String, String> memberNames, List<Expense> expenses) {
        Map<String, Double> totalPaidMap = new HashMap<>();
        Map<String, Double> totalShareMap = new HashMap<>();

        // Initialize maps
        for (String mId : memberIds) {
            totalPaidMap.put(mId, 0.0);
            totalShareMap.put(mId, 0.0);
        }

        // Aggregate expenses
        if (expenses != null) {
            for (Expense exp : expenses) {
                String paidById = exp.getPaidById();
                double amount = exp.getAmount();

                if (totalPaidMap.containsKey(paidById)) {
                    totalPaidMap.put(paidById, totalPaidMap.get(paidById) + amount);
                }

                Map<String, Double> splits = exp.getSplits();
                if (splits != null) {
                    for (Map.Entry<String, Double> entry : splits.entrySet()) {
                        String memberId = entry.getKey();
                        double share = entry.getValue();
                        if (totalShareMap.containsKey(memberId)) {
                            totalShareMap.put(memberId, totalShareMap.get(memberId) + share);
                        }
                    }
                }
            }
        }

        // Build result
        List<Balance> result = new ArrayList<>();
        for (String mId : memberIds) {
            String name = memberNames != null ? memberNames.get(mId) : mId;
            if (name == null) name = mId;

            double paid = round(totalPaidMap.get(mId));
            double share = round(totalShareMap.get(mId));
            result.add(new Balance(mId, name, paid, share));
        }

        return result;
    }

    public static List<PaymentDebt> settleUp(List<Balance> balances) {
        List<PaymentDebt> settlements = new ArrayList<>();
        if (balances == null || balances.isEmpty()) {
            return settlements;
        }

        List<Balance> debtors = new ArrayList<>();
        List<Balance> creditors = new ArrayList<>();

        for (Balance b : balances) {
            double net = round(b.getNetBalance());
            if (net < -0.01) {
                debtors.add(new Balance(b.getMemberId(), b.getMemberName(), b.getTotalPaid(), b.getTotalShare()));
            } else if (net > 0.01) {
                creditors.add(new Balance(b.getMemberId(), b.getMemberName(), b.getTotalPaid(), b.getTotalShare()));
            }
        }

        int dIndex = 0;
        int cIndex = 0;

        while (dIndex < debtors.size() && cIndex < creditors.size()) {
            Balance debtor = debtors.get(dIndex);
            Balance creditor = creditors.get(cIndex);

            double debtVal = Math.abs(round(debtor.getNetBalance()));
            double creditVal = round(creditor.getNetBalance());

            if (debtVal <= 0.01) {
                dIndex++;
                continue;
            }
            if (creditVal <= 0.01) {
                cIndex++;
                continue;
            }

            double transaction = Math.min(debtVal, creditVal);
            transaction = round(transaction);

            settlements.add(new PaymentDebt(
                    debtor.getMemberId(), debtor.getMemberName(),
                    creditor.getMemberId(), creditor.getMemberName(),
                    transaction
            ));

            // Update net balances
            debtor.setTotalShare(debtor.getTotalShare() - transaction); // decreases share -> increases netBalance (closer to 0)
            creditor.setTotalPaid(creditor.getTotalPaid() - transaction); // decreases paid -> decreases netBalance (closer to 0)

            if (Math.abs(debtor.getNetBalance()) <= 0.01) {
                dIndex++;
            }
            if (Math.abs(creditor.getNetBalance()) <= 0.01) {
                cIndex++;
            }
        }

        return settlements;
    }
}
