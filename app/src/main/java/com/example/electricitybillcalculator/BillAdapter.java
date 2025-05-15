package com.example.electricitybillcalculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    private final List<Bill> billList;
    private final OnBillListener onBillListener;

    public BillAdapter(List<Bill> billList, OnBillListener onBillListener) {
        this.billList = billList;
        this.onBillListener = onBillListener;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new BillViewHolder(view, onBillListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);
        holder.textView.setText(bill.getMonth() + ": RM" + String.format("%.2f", bill.getFinalCost()));
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;
        OnBillListener onBillListener;

        public BillViewHolder(@NonNull View itemView, OnBillListener onBillListener) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
            this.onBillListener = onBillListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onBillListener.onBillClick(getAdapterPosition());
        }
    }

    public interface OnBillListener {
        void onBillClick(int position);
    }
}
