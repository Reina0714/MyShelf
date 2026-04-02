package com.example.myshelf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private DatabaseHelper dbHelper;

    private TextView tvUserNameHome;
    private ImageView ivSettingsHome;
    private SharedPreferences sharedPreferences;

    // empty constructor
    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Initialize UI components
        tvUserNameHome = view.findViewById(R.id.tv_user_name_home);
        ivSettingsHome = view.findViewById(R.id.iv_settings_home);

        recyclerView = view.findViewById(R.id.recyclerView_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize SharedPreferences matching SettingsActivity
        sharedPreferences = requireActivity().getSharedPreferences("MyShelfPrefs", Context.MODE_PRIVATE);

        // Open settings page
        ivSettingsHome.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        // Open add item page
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddItemActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadData();
        updateWelcomeMessage();
    }

    private void loadData() {
        if (dbHelper != null) {
            List<InventoryItem> activeItems = dbHelper.getActiveInventory();

            // re-initialize adapter
            if (adapter == null) {
                adapter = new InventoryAdapter(requireContext(), activeItems);
                recyclerView.setAdapter(adapter);
            } else {
                adapter = new InventoryAdapter(requireContext(), activeItems);
                recyclerView.setAdapter(adapter);
            }
        }
    }

    // Get username from SharedPreferences
    private void updateWelcomeMessage() {
        if (sharedPreferences != null && tvUserNameHome != null) {
            String userName = sharedPreferences.getString("USER_NAME", "Friend");

            if (!userName.isEmpty()) {
                tvUserNameHome.setText(userName + "!");
            } else {
                tvUserNameHome.setText("!");
            }
        }
    }
}