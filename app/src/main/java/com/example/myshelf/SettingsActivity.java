package com.example.myshelf;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SettingsActivity extends AppCompatActivity {

    private EditText etUserName;
    private Button btnUpdateName;
    private ImageView ivBack;

    //
    private CardView cvNotifications;
    private TextView tvNotificationStatus;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Bind UI components
        etUserName = findViewById(R.id.et_user_name);
        btnUpdateName = findViewById(R.id.btn_update_name);
        ivBack = findViewById(R.id.iv_back_settings);

        cvNotifications = findViewById(R.id.cv_notifications);
        tvNotificationStatus = findViewById(R.id.tv_notification_status);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyShelfPrefs", MODE_PRIVATE);

        // Load saved name
        String savedName = sharedPreferences.getString("USER_NAME", "");
        etUserName.setText(savedName);

        btnUpdateName.setOnClickListener(v -> {
            String newName = etUserName.getText().toString().trim();
            if (!newName.isEmpty()) {
                sharedPreferences.edit().putString("USER_NAME", newName).apply();
                Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        ivBack.setOnClickListener(v -> finish());

        // Notification preference
        int savedDays = sharedPreferences.getInt("NOTIFY_DAYS", 2);
        updateNotificationText(savedDays);

        cvNotifications.setOnClickListener(v -> {
            // Always fetch the latest value before showing dialog
            int latestDays = sharedPreferences.getInt("NOTIFY_DAYS", 2);
            showNotificationDialog(latestDays);
        });
    }

    // Fetch latest saved days from SharedPreferences
    private void showNotificationDialog(int currentSelectedDays) {
        String[] options = {"1 day before", "2 days before", "3 days before", "1 week before"};

        // map saved days to dialog index
        int checkedItem;
        if (currentSelectedDays == 1) checkedItem = 0;
        else if (currentSelectedDays == 3) checkedItem = 2;
        else if (currentSelectedDays == 7) checkedItem = 3;
        else checkedItem = 1; // Default to 2 days

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remind me...");

        builder.setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
            int newDays;
            if (which == 0) newDays = 1;
            else if (which == 2) newDays = 3;
            else if (which == 3) newDays = 7;
            else newDays = 2; // Default option

            // Save to SharedPreferences
            sharedPreferences.edit().putInt("NOTIFY_DAYS", newDays).apply();
            updateNotificationText(newDays);

            dialog.dismiss();
            Toast.makeText(this, "Notification preference saved!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Update status text
    private void updateNotificationText(int days) {
        if (days == 7) {
            tvNotificationStatus.setText("1 week before");
        } else if (days == 1) {
            tvNotificationStatus.setText("1 day before");
        } else {
            tvNotificationStatus.setText(days + " days before");
        }
    }
}