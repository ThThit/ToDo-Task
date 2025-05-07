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
    // Use channel ID constant from MainActivity or define here
    private static final String CHANNEL_ID = "task_reminder_channel";

    // Action identifiers for BroadcastReceiver
    private static final String ACTION_DISMISS = "com.project.todotasks.ACTION_DISMISS";
    private static final String ACTION_DONE = "com.project.todotasks.ACTION_DONE";

    // unique request code for each notification's actions
    private static final int DISMISS_REQUEST_CODE_BASE = 1000;
    private static final int DONE_REQUEST_CODE_BASE = 2000;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);

        Log.d(TAG, "Received alarm for task: " + taskTitle);

        if (taskTitle == null) {
            Log.e(TAG, "Received alarm intent with null task title.");
            taskTitle = "Task Reminder"; // Fallback text
        }

        if (ACTION_DISMISS.equals(action)){
            // cancel notification
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null){
                notificationManager.cancelAll();
            }
            return;
        }

        if (ACTION_DONE.equals(action)){
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null){
                notificationManager.cancelAll();
            }
            return;
        }

        // Intent to launch MainActivity when notification is clicked
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(), // Unique request code for this PendingIntent
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent to dismiss the notification
        Intent dismissIntent = new Intent(context, NotificationReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS);
        dismissIntent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                DISMISS_REQUEST_CODE_BASE + (int) System.currentTimeMillis(),
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent doneIntent = new Intent(context, NotificationReceiver.class);
        doneIntent.setAction(ACTION_DONE);
        doneIntent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        PendingIntent donePendingIntent = PendingIntent.getBroadcast(
                context,
                DISMISS_REQUEST_CODE_BASE + (int) System.currentTimeMillis(),
                doneIntent,
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

        builder.addAction(R.drawable.ic_delete_x,"Dismiss", dismissPendingIntent);
        builder.addAction(R.drawable.ic_done, "Done", donePendingIntent);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis(); // Simple approach for now
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification displayed for task: " + taskTitle + " with ID: " + notificationId);
        } else {
            Log.e(TAG, "NotificationManager is null, cannot display notification.");
        }
    }
}