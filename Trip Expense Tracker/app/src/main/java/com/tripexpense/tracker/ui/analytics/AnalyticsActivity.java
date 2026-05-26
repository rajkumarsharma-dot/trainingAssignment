package com.tripexpense.tracker.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.model.Expense;
import com.tripexpense.tracker.model.Trip;
import com.tripexpense.tracker.util.CalculationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private Trip trip;
    private List<Expense> expensesList;
    private PieChart pieChart;
    private LinearLayout llPayerContributions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        trip = (Trip) getIntent().getSerializableExtra("TRIP");
        expensesList = (List<Expense>) getIntent().getSerializableExtra("EXPENSES");

        if (trip == null || expensesList == null) {
            Toast.makeText(this, "Failed to load analytics context.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pieChart = findViewById(R.id.pie_chart_category);
        llPayerContributions = findViewById(R.id.ll_payer_contributions);

        findViewById(R.id.btn_analytics_back).setOnClickListener(v -> finish());

        renderPieChart();
        renderMemberContributions();
    }

    private void renderPieChart() {
        if (expensesList.isEmpty()) {
            pieChart.setNoDataText("No expenses logged to display.");
            pieChart.invalidate();
            return;
        }

        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpense = 0.0;
        for (Expense exp : expensesList) {
            String cat = exp.getCategory() != null ? exp.getCategory() : "Other";
            double amt = exp.getAmount();
            totalExpense += amt;
            categoryTotals.put(cat, categoryTotals.containsKey(cat) ? categoryTotals.get(cat) + amt : amt);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        int[] paletteColors = {
                ContextCompat.getColor(this, R.color.primary),
                ContextCompat.getColor(this, R.color.secondary),
                ContextCompat.getColor(this, R.color.success),
                ContextCompat.getColor(this, R.color.accent),
                ContextCompat.getColor(this, R.color.error),
                ContextCompat.getColor(this, R.color.warning),
                ContextCompat.getColor(this, R.color.primary_dark)
        };
        List<Integer> colors = new ArrayList<>();
        for (int c : paletteColors) {
            colors.add(c);
        }
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(50f);
        pieChart.setCenterText(String.format(Locale.getDefault(), "Total\n₹%,.0f", totalExpense));
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.primary));
        pieChart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark));
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void renderMemberContributions() {
        llPayerContributions.removeAllViews();

        Map<String, Double> payerPaidMap = new HashMap<>();
        double totalExpenseSum = 0.0;
        
        for (String mId : trip.getMemberIds()) {
            payerPaidMap.put(mId, 0.0);
        }

        for (Expense exp : expensesList) {
            String paidById = exp.getPaidById();
            double amt = exp.getAmount();
            totalExpenseSum += amt;

            if (payerPaidMap.containsKey(paidById)) {
                payerPaidMap.put(paidById, payerPaidMap.get(paidById) + amt);
            }
        }

        for (String mId : trip.getMemberIds()) {
            String name = trip.getMemberNames().get(mId);
            if (name == null) name = mId;

            double paidAmount = CalculationUtil.round(payerPaidMap.get(mId));

            View row = getLayoutInflater().inflate(R.layout.item_trip, llPayerContributions, false);
            
            row.findViewById(R.id.tv_trip_status).setVisibility(View.GONE);
            row.findViewById(R.id.tv_trip_description).setVisibility(View.GONE);

            TextView tvName = row.findViewById(R.id.tv_trip_title);
            tvName.setText(name);
            tvName.setTextSize(15sp);

            TextView tvPaidStatus = row.findViewById(R.id.tv_trip_members_count);
            tvPaidStatus.setText(String.format(Locale.getDefault(), "Paid: ₹%,.2f", paidAmount));
            tvPaidStatus.setTextColor(ContextCompat.getColor(this, R.color.primary));

            TextView tvTotalContribution = row.findViewById(R.id.tv_trip_total_expenses);
            tvTotalContribution.setText("");

            TextView tvFraction = row.findViewById(R.id.tv_trip_budget);
            
            double fraction = 0.0;
            if (totalExpenseSum > 0) {
                fraction = (paidAmount / totalExpenseSum) * 100;
            }
            tvFraction.setText(String.format(Locale.getDefault(), "%.1f%% of Trip", fraction));
            tvFraction.setTextSize(11sp);

            LinearProgressIndicator pbContribution = row.findViewById(R.id.pb_trip_budget);
            pbContribution.setProgress((int) fraction);
            pbContribution.setIndicatorColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary)));
            pbContribution.setTrackColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary_light)));

            com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) row;
            card.setStrokeWidth(0);
            card.setCardElevation(2);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
            params.setMargins(0, 8, 0, 8);
            card.setLayoutParams(params);

            llPayerContributions.addView(row);
        }
    }
}
