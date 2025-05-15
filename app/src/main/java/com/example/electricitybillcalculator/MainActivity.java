package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.text.DecimalFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView monthSpinner;
    private TextInputEditText unitsEditText;
    private MaterialButtonToggleGroup rebateToggleGroup;
    private MaterialTextView totalChargesTextView, finalCostTextView;
    private MaterialButton saveButton;

    private double totalCharges = 0;
    private double finalCost = 0;

    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "electricity-bill-db").build();

        // Initialize views
        monthSpinner = findViewById(R.id.monthSpinner);
        unitsEditText = findViewById(R.id.unitsEditText);
        rebateToggleGroup = findViewById(R.id.rebateToggleGroup);
        totalChargesTextView = findViewById(R.id.totalChargesTextView);
        finalCostTextView = findViewById(R.id.finalCostTextView);
        MaterialButton calculateButton = findViewById(R.id.calculateButton);
        saveButton = findViewById(R.id.saveButton);
        MaterialButton viewBillsButton = findViewById(R.id.viewBillsButton);
        MaterialButton aboutButton = findViewById(R.id.aboutButton);

        // Setup month dropdown
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, R.layout.dropdown_menu_item);
        monthSpinner.setAdapter(monthAdapter);

        // Calculate button click
        calculateButton.setOnClickListener(v -> calculateBill());

        // Save button click
        saveButton.setOnClickListener(v -> saveBill());

        // View bills button click
        viewBillsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BillListActivity.class);
            startActivity(intent);
        });

        // About button click
        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void calculateBill() {
        String unitsStr = unitsEditText.getText().toString().trim();
        String month = monthSpinner.getText().toString().trim();

        if (month.isEmpty()) {
            Toast.makeText(this, "Please select a month", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unitsStr.isEmpty()) {
            Toast.makeText(this, "Please enter units used", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double units = Double.parseDouble(unitsStr);
            if (units <= 0) {
                Toast.makeText(this, "Units must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected rebate percentage
            int selectedId = rebateToggleGroup.getCheckedButtonId();
            MaterialButton selectedButton = findViewById(selectedId);
            String rebateText = selectedButton.getText().toString();
            double rebatePercentage = Double.parseDouble(rebateText.replace("%", "")) / 100;

            // Calculate charges
            totalCharges = calculateCharges(units);
            finalCost = totalCharges - (totalCharges * rebatePercentage);

            // Display results
            DecimalFormat df = new DecimalFormat("#.00");
            totalChargesTextView.setText("Total Charges: RM" + df.format(totalCharges));
            finalCostTextView.setText("Final Cost: RM" + df.format(finalCost));

            // Enable save button
            saveButton.setEnabled(true);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for units", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateCharges(double units) {
        if (units <= 200) {
            return units * 0.218;
        } else if (units <= 300) {
            return (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            return (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else {
            return (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }
    }

    private void saveBill() {
        String month = monthSpinner.getText().toString().trim();
        String unitsStr = unitsEditText.getText().toString().trim();

        if (month.isEmpty() || unitsStr.isEmpty() || totalCharges == 0) {
            Toast.makeText(this, "Please calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        double units = Double.parseDouble(unitsStr);
        int selectedId = rebateToggleGroup.getCheckedButtonId();
        MaterialButton selectedButton = findViewById(selectedId);
        String rebateText = selectedButton.getText().toString();
        double rebatePercentage = Double.parseDouble(rebateText.replace("%", ""));

        Bill bill = new Bill(month, units, rebatePercentage, totalCharges, finalCost);

        executor.execute(() -> {
            db.billDao().insert(bill);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Bill saved successfully", Toast.LENGTH_SHORT).show();
                resetForm();
            });
        });
    }

    private void resetForm() {
        unitsEditText.setText("");
        totalCharges = 0;
        finalCost = 0;
        totalChargesTextView.setText("Total Charges: RM0.00");
        finalCostTextView.setText("Final Cost: RM0.00");
        saveButton.setEnabled(false);
    }
}