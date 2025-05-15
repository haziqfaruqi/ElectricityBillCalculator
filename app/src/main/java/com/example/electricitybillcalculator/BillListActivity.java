package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BillListActivity extends AppCompatActivity implements BillAdapter.OnBillListener {

    private RecyclerView recyclerView;
    private BillAdapter adapter;
    private List<Bill> billList = new ArrayList<>();

    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);

        // Initialize database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "electricity-bill-db").build();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.billsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BillAdapter(billList, this);
        recyclerView.setAdapter(adapter);

        // Load bills from database
        loadBills();
    }

    private void loadBills() {
        executor.execute(() -> {
            List<Bill> bills = db.billDao().getAllBills();
            runOnUiThread(() -> {
                billList.clear();
                billList.addAll(bills);
                adapter.notifyDataSetChanged();

                if (billList.isEmpty()) {
                    Toast.makeText(this, "No saved bills found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onBillClick(int position) {
        Bill selectedBill = billList.get(position);
        Intent intent = new Intent(this, BillDetailActivity.class);
        intent.putExtra("BILL_ID", selectedBill.getId());
        startActivity(intent);
    }
}