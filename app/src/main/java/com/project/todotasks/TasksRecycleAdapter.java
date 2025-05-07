package com.project.todotasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TasksRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "TasksRecycleAdapter";
    private static final String SHARED_PREFS_NAME = "MyTasksPrefs";
    private static final String TASK_LIST_KEY = "task_list";
    private static final String COMPLETE_TASK_LIST_KEY = "complete_task_list";

    private ArrayList<TaskList> tasks; // Keep local copy for adapter
    private ArrayList<CompleteTasks> completeTasks;
    private final Context context;
    private final FragmentActivity activity; // Host activity for showing dialogs
    private List<?> currentDisplayedTasks;
    // choosing layout
    private static final int VIEW_TYPE_ONGOING = 0;
    private static final int VIEW_TYPE_COMPLETED = 1;

    // Constructor takes initial list
    public TasksRecycleAdapter(Context context, FragmentActivity activity, ArrayList<TaskList> onGoingTasks, ArrayList<CompleteTasks> completeTasks) {
        this.context = context;
        this.activity = activity;
        this.tasks = new ArrayList<>(onGoingTasks);
        this.completeTasks = new ArrayList<>(completeTasks); // Create a mutable copy
        this.currentDisplayedTasks = onGoingTasks; // display ongoing tasks initially
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_COMPLETED){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.complete_task_list, parent, false);
            return new CompletedTaskViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list, parent, false);
            return new OnGoingTaskViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder  instanceof CompletedTaskViewHolder){
            CompleteTasks completeTask = (CompleteTasks) currentDisplayedTasks.get(position);
            CompletedTaskViewHolder completeHolder = (CompletedTaskViewHolder) holder;
            completeHolder.taskTitle.setText(completeTask.getTaskTitle());
            completeHolder.taskTitle.setPaintFlags(completeHolder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            completeHolder.taskDate.setText(formatDisplayDate(completeTask.getTaskDate()));
            completeHolder.taskTime.setText(formatDisplayTime(completeTask.getTaskTime()));
            // include completion date
        } else if (holder instanceof OnGoingTaskViewHolder) {
            TaskList task = (TaskList) currentDisplayedTasks.get(position);
            OnGoingTaskViewHolder ongoingHolder = (OnGoingTaskViewHolder) holder;
            ongoingHolder.taskTitle.setText(task.getTaskTitle());
            ongoingHolder.taskDate.setText(formatDisplayDate(task.getTaskDateString()));
            ongoingHolder.taskTime.setText(formatDisplayTime(task.getTaskTimeString()));

            // Click listener for editing the task
            ongoingHolder.parent.setOnClickListener(view -> {
                int currentPosition = ongoingHolder.getAdapterPosition(); // Use getAdapterPosition()
                if (currentPosition != RecyclerView.NO_POSITION  && getItemViewType(currentPosition) == VIEW_TYPE_ONGOING) {
                    TaskList taskToEdit = tasks.get(currentPosition);
                    Log.d(TAG, "Editing task at pos " + currentPosition + ": " + taskToEdit.getTaskTitle());

                    CreateTasksFragment taskEditDialog = new CreateTasksFragment();
                    taskEditDialog.setTaskToEdit(taskToEdit, currentPosition); // Pass task and index
                    notifyDataSetChanged();
                    taskEditDialog.show(activity.getSupportFragmentManager(), "EditTaskDialog");
                }
            });

            // Click listener for marking task as done
            if (ongoingHolder.btnTaskDone != null){
                ongoingHolder.btnTaskDone.setOnClickListener(view -> {
                    int currentPosition = ongoingHolder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        Log.d(TAG, "Deleting task at pos " + currentPosition);

                        // Cancel the Alarm BEFORE removing ---
                        cancelAlarm(context, currentPosition);

                        // Remove from adapter's list
                        TaskList removedTask = tasks.remove(currentPosition);

                        Date now = new Date();
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        String currentTime = timeFormat.format(now);

                        // add to complete task
                        CompleteTasks completeTask =  new CompleteTasks(
                                removedTask.getTaskTitle(),
                                removedTask.getTaskTimeString(),
                                removedTask.getTaskDateString());
                        completeTasks.add(completeTask);
                        saveCompleteTasks(completeTasks);

                        notifyItemRemoved(currentPosition);
                        // Notify subsequent items about position change
                        notifyItemRangeChanged(currentPosition, tasks.size() - currentPosition);

                        // Save the updated list  to SharedPreferences
                        saveTasks(tasks);
                        Toast.makeText(context, "Task '" + removedTask.getTaskTitle() + "' Done at " + currentTime , Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }


    // get the type of current list
    @Override
    public int getItemViewType(int position) {
        Object item = currentDisplayedTasks.get(position);
        if (item instanceof CompleteTasks) {
            return VIEW_TYPE_COMPLETED;
        } else {
            return VIEW_TYPE_ONGOING;
        }
    }

    @Override
    public int getItemCount() {
        return currentDisplayedTasks.size();
    }


    // Set a new list of tasks (e.g., on initial load)
    public void setTasks(ArrayList<TaskList> newTasks) {
        this.tasks = new ArrayList<>(newTasks); // Use a copy
        notifyDataSetChanged(); // Full refresh needed when list is replaced
        Log.d(TAG, "Adapter tasks updated, count: " + tasks.size());
    }

    // update the tasks being display
    private void setDisplayTasks(ArrayList<?> tasks){
        notifyDataSetChanged();
        this.currentDisplayedTasks = tasks;
         // notify data set has changed
    }

    public void showCompletedTasks() {
        setDisplayTasks(completeTasks);
        notifyDataSetChanged();
    }

    public void showOngoingTasks() {
        setDisplayTasks(tasks);
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
        notifyDataSetChanged();
    }

    private void saveCompleteTasks(ArrayList<CompleteTasks> completeTasks){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(completeTasks);
        Log.d(TAG, "Saving complete tasks from adapter: " + json);
        editor.putString(COMPLETE_TASK_LIST_KEY, json);
        editor.apply();
    }

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

    public void setCompleteTasks(ArrayList<CompleteTasks> completeTasks) {
        this.completeTasks = completeTasks;
    }

    // --- ViewHolder ---

    // view holder for ongoing tasks
    public static class OnGoingTaskViewHolder extends RecyclerView.ViewHolder{
        private final TextView taskTitle, taskTime, taskDate;
        View parent;
        FloatingActionButton btnTaskDone;
        public OnGoingTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView;
            taskTitle = itemView.findViewById(R.id.task);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskDate = itemView.findViewById(R.id.taskDate);
            btnTaskDone = itemView.findViewById(R.id.btnTaskDone);
        }
    }

    // view holder for completed tasks
    public static class CompletedTaskViewHolder extends RecyclerView.ViewHolder{
        private final TextView taskTitle, taskTime, taskDate;
        View parent;
        FloatingActionButton btnTaskDone;
        public CompletedTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView;
            taskTitle = itemView.findViewById(R.id.task);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskDate = itemView.findViewById(R.id.taskDate);
            btnTaskDone = itemView.findViewById(R.id.btnTaskDone);
        }
    }
}