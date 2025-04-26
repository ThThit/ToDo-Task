package com.project.todotasks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    // Use a constant for the extra key (match with MainActivity)
    public static final String EXTRA_TASK_TITLE = "com.project.todotasks.TASK_TITLE";
    public static final String EXTRA_TASK_ID = "com.project.todotasks.TASK_ID"; // For future use

    // Use channel ID constant from MainActivity or define here
    private static final String CHANNEL_ID = "task_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        // long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1); // Get ID if implemented

        Log.d(TAG, "Received alarm for task: " + taskTitle);

        if (taskTitle == null) {
            Log.e(TAG, "Received alarm intent with null task title.");
            taskTitle = "Task Reminder"; // Fallback text
        }

        // Intent to launch MainActivity when notification is clicked
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Optional: Add extras to MainActivityIntent if you want to scroll to the task, etc.
        // mainActivityIntent.putExtra("FOCUS_TASK_ID", taskId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(), // Unique request code for this PendingIntent
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true) // Dismiss notification when clicked
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE) // Use default sound & vibrate
                .setContentIntent(pendingIntent); // Set the intent to execute on click

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Use a consistent or task-specific ID if you might need to update/cancel this specific notification later
            // Using current time is okay for simple cases but makes later cancellation harder.
            // int notificationId = (taskId != -1) ? (int) taskId : (int) System.currentTimeMillis();
            int notificationId = (int) System.currentTimeMillis(); // Simple approach for now
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification displayed for task: " + taskTitle + " with ID: " + notificationId);
        } else {
            Log.e(TAG, "NotificationManager is null, cannot display notification.");
        }
    }
}