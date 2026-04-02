package com.example.myshelf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<HistoryItem> historyList;

    public HistoryAdapter(Context context, List<HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem currentItem = historyList.get(position);

        holder.tvHistoryName.setText(currentItem.getItemName());
        holder.tvHistoryDate.setText(currentItem.getActionDate());

        // Format price display
        String priceText = String.format(Locale.getDefault(), "RM %.2f", currentItem.getPriceLoss());
        holder.tvHistoryPrice.setText(priceText);

        // change colour based on item status
        if ("Consumed".equals(currentItem.getFinalStatus())) {
            // consumed = green
            holder.tvHistoryPrice.setBackgroundResource(R.drawable.background_green);
        } else {
            // wasted = red
            holder.tvHistoryPrice.setBackgroundResource(R.drawable.background_red);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryName, tvHistoryDate, tvHistoryPrice;
        ImageView ivHistoryIcon;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryName = itemView.findViewById(R.id.tv_history_name);
            tvHistoryDate = itemView.findViewById(R.id.tv_history_date);
            tvHistoryPrice = itemView.findViewById(R.id.tv_history_price);
            ivHistoryIcon = itemView.findViewById(R.id.iv_history_icon);
        }
    }
}