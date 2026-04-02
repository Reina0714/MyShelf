package com.example.myshelf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TextView tvTotalSavedAmount, tvTotalWastedAmount;
    private DatabaseHelper dbHelper;
    private HistoryAdapter adapter;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rv_history);
        tvTotalSavedAmount = view.findViewById(R.id.tv_total_saved_amount);
        tvTotalWastedAmount = view.findViewById(R.id.tv_total_wasted_amount);

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        dbHelper = new DatabaseHelper(requireContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryAndCalculate(); // Refresh the history list
    }

    private void loadHistoryAndCalculate() {
        List<HistoryItem> historyList = dbHelper.getAllHistory();

        double totalSaved = 0.0;
        double totalWasted = 0.0;

        // Aggregate total amounts by status
        for (HistoryItem item : historyList) {
            if ("Consumed".equals(item.getFinalStatus())) {
                totalSaved += item.getPriceLoss();
            } else if ("Wasted".equals(item.getFinalStatus())) {
                totalWasted += item.getPriceLoss();
            }
        }

        // update the result to UI
        tvTotalSavedAmount.setText(String.format(Locale.getDefault(), "RM %.2f", totalSaved));
        tvTotalWastedAmount.setText(String.format(Locale.getDefault(), "RM %.2f", totalWasted));

        // update the adapter
        adapter = new HistoryAdapter(requireContext(), historyList);
        rvHistory.setAdapter(adapter);
    }
}