package com.project.todotasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity; // Needed to show dialog
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TasksRecycleAdapter extends RecyclerView.Adapter<TasksRecycleAdapter.ViewHolder> {

    private static final String TAG = "TasksRecycleAdapter";
    private static final String SHARED_PREFS_NAME = "MyTasksPrefs";
    private static final String TASK_LIST_KEY = "task_list";

    private ArrayList<TaskList> tasks; // Keep local copy for adapter
    private final Context context;
    private final FragmentActivity activity; // Host activity for showing dialogs

    // Constructor takes initial list
    public TasksRecycleAdapter(Context context, FragmentActivity activity, ArrayList<TaskList> initialTasks) {
        this.context = context;
        this.activity = activity;
        this.tasks = new ArrayList<>(initialTasks); // Create a mutable copy
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskList taskList = tasks.get(position);
        holder.taskTitle.setText(taskList.getTaskTitle());

        holder.taskDate.setText(formatDisplayDate(taskList.getTaskDateString()));
        holder.taskTime.setText(formatDisplayTime(taskList.getTaskTimeString()));

        // Click listener for editing the task
        holder.parent.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition(); // Use getAdapterPosition()
            if (currentPosition != RecyclerView.NO_POSITION) {
                TaskList taskToEdit = tasks.get(currentPosition);
                Log.d(TAG, "Editing task at pos " + currentPosition + ": " + taskToEdit.getTaskTitle());

                CreateTasksFragment taskEditDialog = new CreateTasksFragment();
                taskEditDialog.setTaskToEdit(taskToEdit, currentPosition); // Pass task and index

                taskEditDialog.show(activity.getSupportFragmentManager(), "EditTaskDialog");
            }
        });

        // Click listener for marking task as done (deleting)
        holder.btnTaskDone.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Log.d(TAG, "Deleting task at pos " + currentPosition);

                // --- IMPORTANT: Cancel the Alarm BEFORE removing ---
                cancelAlarm(context, currentPosition);

                // Remove from adapter's list
                TaskList removedTask = tasks.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                // Notify subsequent items about position change
                notifyItemRangeChanged(currentPosition, tasks.size() - currentPosition);


                // Save the updated list (without the removed task) to SharedPreferences
                saveTasks(tasks); // Save the adapter's current list state

                Toast.makeText(context, "Task '" + removedTask.getTaskTitle() + "' Done", Toast.LENGTH_SHORT).show();

                // Note: We are now saving the adapter's list. No need to load/modify/save separately.
                // Make sure MainActivity's list is also updated if it's used elsewhere directly.
                // Currently, MainActivity reloads on start and gets updates via listener, which should be okay.
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // --- Data Modification Methods ---

    // Method called by MainActivity when a task is added
    public void addTask(TaskList task) {
        tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    // Method called by MainActivity when a task is updated
    public void updateTask(TaskList updatedTask, int position) {
        if (position >= 0 && position < tasks.size()) {
            tasks.set(position, updatedTask);
            notifyItemChanged(position);
        }
    }

    // Set a new list of tasks (e.g., on initial load)
    public void setTasks(ArrayList<TaskList> newTasks) {
        this.tasks = new ArrayList<>(newTasks); // Use a copy
        notifyDataSetChanged(); // Full refresh needed when list is replaced
        Log.d(TAG, "Adapter tasks updated, count: " + tasks.size());
    }


    // --- SharedPreferences Persistence (handled within Adapter for delete consistency) ---

    private void saveTasks(ArrayList<TaskList> taskLists) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskLists);
        Log.d(TAG, "Saving tasks from adapter: " + json);
        editor.putString(TASK_LIST_KEY, json);
        editor.apply();
    }

    // Load tasks (might not be needed here if MainActivity handles initial load)
    /*
    private ArrayList<TaskList> loadTasks() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(TASK_LIST_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<TaskList>>() {}.getType();
            try {
                 ArrayList<TaskList> loaded = gson.fromJson(json, type);
                 return loaded != null ? loaded : new ArrayList<>();
            } catch (Exception e) {
                Log.e(TAG, "Error loading tasks in adapter", e);
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }
    */


    // --- Alarm Cancellation Helper ---
    private void cancelAlarm(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null, cannot cancel alarm.");
            return;
        }
        Intent intent = new Intent(context, NotificationReceiver.class); // Match the intent used for scheduling

        // Recreate the EXACT SAME PendingIntent to cancel it
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode, // Use the same request code (position)
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Use same flags
        );

        // Cancel the alarm
        alarmManager.cancel(pendingIntent);
        // Cancel the PendingIntent itself
        pendingIntent.cancel();

        Log.d(TAG, "Adapter requested alarm cancellation for request code: " + requestCode);
    }


    // --- Formatting Helpers ---

    // Example: Format date for display (e.g., "Apr 26, 2025" or relative "Tomorrow")
    private String formatDisplayDate(String dateString_yyyyMMdd) {
        if (dateString_yyyyMMdd == null) return "No date";
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = parseFormat.parse(dateString_yyyyMMdd);
            if (date == null) return "Invalid date";

            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(date);

            Calendar today = Calendar.getInstance();
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);

            // Reset time parts for date comparison
            today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);
            tomorrow.set(Calendar.HOUR_OF_DAY, 0); tomorrow.set(Calendar.MINUTE, 0); tomorrow.set(Calendar.SECOND, 0); tomorrow.set(Calendar.MILLISECOND, 0);
            taskCal.set(Calendar.HOUR_OF_DAY, 0); taskCal.set(Calendar.MINUTE, 0); taskCal.set(Calendar.SECOND, 0); taskCal.set(Calendar.MILLISECOND, 0);

            if (taskCal.equals(today)) {
                return "Today";
            } else if (taskCal.equals(tomorrow)) {
                return "Tomorrow";
            } else {
                // Example: "Apr 26, 2025"
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting display date: " + dateString_yyyyMMdd, e);
            return dateString_yyyyMMdd; // Fallback
        }
    }

    // Example: Format time for display (e.g., "05:30 PM")
    private String formatDisplayTime(String timeString_HHmm) {
        if (timeString_HHmm == null) return "No time";
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = parseFormat.parse(timeString_HHmm);
            if (date == null) return "Invalid time";

            SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // 12-hour format with AM/PM
            return displayFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting display time: " + timeString_HHmm, e);
            return timeString_HHmm; // Fallback
        }
    }

    // --- ViewHolder ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskTitle, taskTime, taskDate;
        View parent; // The root view of the list item
        FloatingActionButton btnTaskDone; // Assuming FAB for done

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView; // Root view (e.g., CardView or ConstraintLayout)
            taskTitle = itemView.findViewById(R.id.task);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskDate = itemView.findViewById(R.id.taskDate);
            btnTaskDone = itemView.findViewById(R.id.btnTaskDone); // Make sure ID exists in R.layout.task_list
        }
    }
}