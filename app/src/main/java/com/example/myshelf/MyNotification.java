package com.example.myshelf;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MyNotification extends Worker {

    public MyNotification(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // 1. Fetch user preference for notification days (default is 2 days)
        SharedPreferences prefs = context.getSharedPreferences("MyShelfPrefs", Context.MODE_PRIVATE);
        int notifyDays = prefs.getInt("NOTIFY_DAYS", 2);

        // 2. Get active items from the database
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<InventoryItem> activeItems = dbHelper.getActiveInventory();

        int urgentCount = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today = new Date(); // Get current date

        // 3. Calculate days remaining until expiry
        for (InventoryItem item : activeItems) {
            try {
                Date expiryDate = sdf.parse(item.getExpiryDate());
                if (expiryDate != null) {
                    // Convert date difference to days
                    long diffInMillies = expiryDate.getTime() - today.getTime();
                    long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    // count items expiring soon or already expired
                    if (diffInDays <= notifyDays) {
                        urgentCount++;
                    }
                }
            } catch (ParseException e) {
                // skip the items with invalid date format
                e.printStackTrace();
            }
        }

        // 4. Send notification only if there are urgent items
        if (urgentCount > 0) {
            sendNotification("MyShelf Alert", "You have " + urgentCount + " items expiring soon or already expired!");
        }

        return Result.success();
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "expiry_alerts";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Expiry Alerts", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_settings) // Placeholder icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}