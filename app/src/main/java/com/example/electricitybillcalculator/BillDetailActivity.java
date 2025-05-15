package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BillDetailActivity extends AppCompatActivity {

    private TextView monthTextView, unitsTextView, rebateTextView,
            totalChargesTextView, finalCostTextView;

    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        // Initialize views
        monthTextView = findViewById(R.id.monthTextView);
        unitsTextView = findViewById(R.id.unitsTextView);
        rebateTextView = findViewById(R.id.rebateTextView);
        totalChargesTextView = findViewById(R.id.totalChargesTextView);
        finalCostTextView = findViewById(R.id.finalCostTextView);

        // Initialize database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "electricity-bill-db").build();

        // Get bill ID from intent
        int billId = getIntent().getIntExtra("BILL_ID", -1);

        if (billId != -1) {
            loadBillDetails(billId);
        } else {
            Toast.makeText(this, "Error loading bill details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadBillDetails(int billId) {
        executor.execute(() -> {
            Bill bill = db.billDao().getBillById(billId);
            runOnUiThread(() -> {
                if (bill != null) {
                    displayBillDetails(bill);
                } else {
                    Toast.makeText(this, "Bill not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void displayBillDetails(Bill bill) {
        monthTextView.setText("Month: " + bill.getMonth());
        unitsTextView.setText("Units Used: " + bill.getUnits() + " kWh");
        rebateTextView.setText("Rebate: " + bill.getRebate() + "%");
        totalChargesTextView.setText("Total Charges: RM" + String.format("%.2f", bill.getTotalCharges()));
        finalCostTextView.setText("Final Cost: RM" + String.format("%.2f", bill.getFinalCost()));
    }
}