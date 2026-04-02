package com.example.myshelf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private Context context;
    private List<InventoryItem> inventoryList;

    public InventoryAdapter(Context context, List<InventoryItem> inventoryList) {
        this.context = context;
        this.inventoryList = inventoryList;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem currentItem = inventoryList.get(position);

        // Set item name
        holder.tvItemName.setText(currentItem.getItemName());

        // set icon based on category
        String category = currentItem.getCategory();

        if (category == null) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_others);
        } else if (category.equalsIgnoreCase("Dairy")) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_dairy);
        } else if (category.equalsIgnoreCase("Vegetable")) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_vege);
        } else if (category.equalsIgnoreCase("Meat")) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_meat);
        } else if (category.equalsIgnoreCase("Fruit")) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_fruit);
        } else if (category.equalsIgnoreCase("Noodle")) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_noodle);
        } else if (category.equalsIgnoreCase("Egg")) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_egg);
        } else if (category.equalsIgnoreCase("Others")) {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_others);
        } else {
            holder.ivItemIcon.setImageResource(R.drawable.ic_ctg_others);
        }

        String description = currentItem.getQuantity() + " Remaining";
        holder.tvItemDescription.setText(description);

        String priceText = String.format(Locale.getDefault(), "RM %.2f", currentItem.getPrice());
        holder.tvItemPrice.setText(priceText);

        // Calculate expiry status and colour
        String expiryDateStr = currentItem.getExpiryDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date expiryDate = sdf.parse(expiryDateStr);
            Date today = sdf.parse(sdf.format(new Date()));

            if (expiryDate != null) {
                long diffInMillis = expiryDate.getTime() - today.getTime();
                long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                if (diffInDays < 0) {
                    holder.tvItemStatus.setText("EXPIRED");
                    holder.tvItemStatus.setBackgroundResource(R.drawable.background_red);
                    holder.tvExpiryDays.setText("Exp: " + expiryDateStr + " (Expired!)");
                    holder.tvExpiryDays.setTextColor(android.graphics.Color.RED);
                } else if (diffInDays == 0) {
                    holder.tvItemStatus.setText("USE SOON");
                    holder.tvItemStatus.setBackgroundResource(R.drawable.background_yellow);
                    holder.tvExpiryDays.setText("Exp: " + expiryDateStr + " (Today)");
                    holder.tvExpiryDays.setTextColor(android.graphics.Color.parseColor("#FF9800"));
                } else if (diffInDays <= 3) {
                    holder.tvItemStatus.setText("USE SOON");
                    holder.tvItemStatus.setBackgroundResource(R.drawable.background_yellow);
                    holder.tvExpiryDays.setText("Exp: " + expiryDateStr + " (" + diffInDays + " days)");
                    holder.tvExpiryDays.setTextColor(android.graphics.Color.parseColor("#FF9800"));
                } else {
                    holder.tvItemStatus.setText("FRESH");
                    holder.tvItemStatus.setBackgroundResource(R.drawable.background_green);
                    holder.tvExpiryDays.setText("Exp: " + expiryDateStr + " (" + diffInDays + " days)");
                    holder.tvExpiryDays.setTextColor(android.graphics.Color.parseColor("#888888"));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            holder.tvItemStatus.setText(expiryDateStr);
            holder.tvItemStatus.setBackgroundResource(R.drawable.background_green);
            holder.tvExpiryDays.setText("");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionDialog(currentItem, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    // Show action dialog
    private void showActionDialog(InventoryItem item, int position) {
        String[] options = {
                "Consumed",
                "Wasted",
                "Delete"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Manage " + item.getItemName());

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    updateItemStatusInDatabase(item, "Consumed", position);
                } else if (which == 1) {
                    updateItemStatusInDatabase(item, "Wasted", position);
                } else if (which == 2) {
                    deleteItemFromDatabase(item, position);
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // update item status
    private void updateItemStatusInDatabase(InventoryItem item, String status, int position) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        boolean success = dbHelper.markItemAs(item, status);

        if (success) {
            inventoryList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, inventoryList.size());
            Toast.makeText(context, "Marked as " + status, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error updating status", Toast.LENGTH_SHORT).show();
        }
    }

    // Delete item
    private void deleteItemFromDatabase(InventoryItem item, int position) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        boolean success = dbHelper.permanentlyDeleteItem(item);

        if (success) {
            inventoryList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, inventoryList.size());
            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show();
        }
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDescription, tvItemStatus, tvItemPrice;
        ImageView ivItemIcon;
        TextView tvExpiryDays;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemDescription = itemView.findViewById(R.id.tv_item_description);
            tvItemStatus = itemView.findViewById(R.id.tv_item_status);
            tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            ivItemIcon = itemView.findViewById(R.id.iv_item_icon);
            tvExpiryDays = itemView.findViewById(R.id.tv_expiry_days);
        }
    }
}