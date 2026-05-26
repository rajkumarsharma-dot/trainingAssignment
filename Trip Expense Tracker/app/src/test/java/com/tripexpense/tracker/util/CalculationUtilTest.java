package com.tripexpense.tracker.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.tripexpense.tracker.model.Balance;
import com.tripexpense.tracker.model.Expense;
import com.tripexpense.tracker.model.PaymentDebt;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculationUtilTest {

    @Test
    public void testRound() {
        assertEquals(10.33, CalculationUtil.round(10.33333), 0.001);
        assertEquals(10.34, CalculationUtil.round(10.335), 0.001);
        assertEquals(10.30, CalculationUtil.round(10.3), 0.001);
    }

    @Test
    public void testSplitEqually() {
        List<String> members = Arrays.asList("m1", "m2", "m3");
        Map<String, Double> splits = CalculationUtil.splitEqually(100.0, members);

        assertNotNull(splits);
        assertEquals(3, splits.size());

        double total = 0.0;
        for (double val : splits.values()) {
            total += val;
        }

        assertEquals(100.0, total, 0.001);
        // First member should have received the remainder adjustment
        assertEquals(33.34, splits.get("m1"), 0.001);
        assertEquals(33.33, splits.get("m2"), 0.001);
        assertEquals(33.33, splits.get("m3"), 0.001);
    }

    @Test
    public void testCalculateBalances() {
        List<String> members = Arrays.asList("m1", "m2", "m3");
        
        Map<String, String> names = new HashMap<>();
        names.put("m1", "Arjun");
        names.put("m2", "Priya");
        names.put("m3", "Rahul");

        List<Expense> expenses = new ArrayList<>();
        
        // Expense 1: Arjun paid 9000, split equally
        Map<String, Double> splits1 = CalculationUtil.splitEqually(9000.0, members);
        expenses.add(new Expense("e1", "t1", "Paragliding", 9000.0, "m1", "Arjun", "Activities", 0L, "", "EQUAL", splits1));

        // Expense 2: Priya paid 1500, split equally
        Map<String, Double> splits2 = CalculationUtil.splitEqually(1500.0, members);
        expenses.add(new Expense("e2", "t1", "Lunch", 1500.0, "m2", "Priya", "Food", 0L, "", "EQUAL", splits2));

        List<Balance> balances = CalculationUtil.calculateBalances(members, names, expenses);

        assertNotNull(balances);
        assertEquals(3, balances.size());

        // Arjun (m1): paid 9000, share 3000 + 500 = 3500. Net: 5500.
        // Priya (m2): paid 1500, share 3500. Net: -2000.
        // Rahul (m3): paid 0, share 3500. Net: -3500.

        Balance bArjun = getBalanceById(balances, "m1");
        Balance bPriya = getBalanceById(balances, "m2");
        Balance bRahul = getBalanceById(balances, "m3");

        assertNotNull(bArjun);
        assertEquals("Arjun", bArjun.getMemberName());
        assertEquals(9000.0, bArjun.getTotalPaid(), 0.001);
        assertEquals(3500.0, bArjun.getTotalShare(), 0.001);
        assertEquals(5500.0, bArjun.getNetBalance(), 0.001);

        assertNotNull(bPriya);
        assertEquals(1500.0, bPriya.getTotalPaid(), 0.001);
        assertEquals(3500.0, bPriya.getTotalShare(), 0.001);
        assertEquals(-2000.0, bPriya.getNetBalance(), 0.001);

        assertNotNull(bRahul);
        assertEquals(0.0, bRahul.getTotalPaid(), 0.001);
        assertEquals(3500.0, bRahul.getTotalShare(), 0.001);
        assertEquals(-3500.0, bRahul.getNetBalance(), 0.001);
    }

    @Test
    public void testSettleUp() {
        List<Balance> balances = Arrays.asList(
            new Balance("m1", "Arjun", 9000.0, 3500.0), // net = +5500
            new Balance("m2", "Priya", 1500.0, 3500.0), // net = -2000
            new Balance("m3", "Rahul", 0.0, 3500.0)     // net = -3500
        );

        List<PaymentDebt> debts = CalculationUtil.settleUp(balances);

        assertNotNull(debts);
        // With greedy, Rahul owes Arjun 3500, Priya owes Arjun 2000. Total transactions = 2.
        assertEquals(2, debts.size());

        PaymentDebt debt1 = debts.get(0);
        assertEquals("m3", debt1.getDebtorId());
        assertEquals("m1", debt1.getCreditorId());
        assertEquals(3500.0, debt1.getAmount(), 0.001);

        PaymentDebt debt2 = debts.get(1);
        assertEquals("m2", debt2.getDebtorId());
        assertEquals("m1", debt2.getCreditorId());
        assertEquals(2000.0, debt2.getAmount(), 0.001);
    }

    private Balance getBalanceById(List<Balance> list, String id) {
        for (Balance b : list) {
            if (b.getMemberId().equals(id)) {
                return b;
            }
        }
        return null;
    }
}
