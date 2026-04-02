package com.example.myshelf;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddItemActivity extends AppCompatActivity {

    private EditText etItemName, etQuantity, etPrice;
    private Spinner spinnerCategory;
    private TextView tvExpiryDate;
    private Button btnSave;
    private ImageView ivBack;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // 1. Initialize Database Helper
        databaseHelper = new DatabaseHelper(this);

        // 2. Initialize Views
        etItemName = findViewById(R.id.et_item_name);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_price);
        spinnerCategory = findViewById(R.id.spinner_category);
        tvExpiryDate = findViewById(R.id.tv_expiry_date);
        btnSave = findViewById(R.id.btn_save_item);
        ivBack = findViewById(R.id.iv_back);

        // 3. Setup Category Spinner
        String[] categories = {"Dairy", "Vegetable", "Meat", "Fruit", "Noodle", "Egg", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // 4. Date Picker
        tvExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // 5. Close the activity
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close this activity and go back
            }
        });

        // 6. Save Item
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItemToDatabase();
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddItemActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        tvExpiryDate.setText(selectedDate);
                        tvExpiryDate.setTextColor(androidx.core.content.ContextCompat.getColor(AddItemActivity.this, R.color.text_black));
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void saveItemToDatabase() {
        String name = etItemName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String expiryDate = tvExpiryDate.getText().toString();

        // ensure every field is fill in
        if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty() || expiryDate.equals("DD/MM/YYYY")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);

            InventoryItem newItem = new InventoryItem(name, quantity, expiryDate, category, price, "Active");

            // save to database
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            boolean success = dbHelper.insertInventoryItem(newItem);
            if (success) {
                Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for quantity and price", Toast.LENGTH_SHORT).show();
        }
    }
}