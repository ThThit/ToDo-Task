package com.project.todotasks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CreateTasksFragment.TaskDialogListener {

    private static final String TAG = "MainActivity";
    private static final String SHARED_PREFS_NAME = "MyTasksPrefs";
    private static final String TASK_LIST_KEY = "task_list";
    private static final String COMPLETE_TASK_LIST_KEY = "complete_task_list";
    private static final String CHANNEL_ID = "task_reminder_channel";

    // Hardcoded strings instead of resources for Notification Channel
    private static final String NOTIFICATION_CHANNEL_NAME = "Task Reminders";
    private static final String NOTIFICATION_CHANNEL_DESC = "Channel for task reminder notifications";


    private ArrayList<TaskList> tasksList = new ArrayList<>();
    private ArrayList<CompleteTasks> completeTasks = new ArrayList<>();
    private TasksRecycleAdapter tasksAdapter;
    private RecyclerView taskViewRecycle;
    private AlarmManager alarmManager;
    private MaterialButtonToggleGroup filterButtonGroup;
    private Button btnOngoing;
    private Button btnCompleted;

    // --- ActivityResultLaunchers (Permission Handling) ---
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                    checkExactAlarmPermission();
                } else {
                    Toast.makeText(this, "Notification permission denied. Reminders will not work.", Toast.LENGTH_LONG).show();
                    // Handle denial (e.g., explain limitation)
                }
            });

    private final ActivityResultLauncher<Intent> requestExactAlarmLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Exact alarm permission granted.", Toast.LENGTH_SHORT).show();
                    scheduleAllTaskNotifications(this, tasksList);
                } else {
                    Toast.makeText(this, "Exact alarm permission still denied. Reminders may be delayed.", Toast.LENGTH_LONG).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Notification setup
        createNotificationChannel();
        requestNotificationPermissionIfNeeded();

        taskViewRecycle = findViewById(R.id.taskView);
        ExtendedFloatingActionButton fabAddTask = findViewById(R.id.btnNewTask);
        filterButtonGroup = findViewById(R.id.filterButtonGroup);
        btnOngoing = findViewById(R.id.btnOngoing);
        btnCompleted = findViewById(R.id.btnCompleted);

        tasksList = loadTasks();
        completeTasks = loadCompleteTasks();
        Log.d(TAG, "Loaded " + tasksList.size() + " tasks.");

        tasksAdapter = new TasksRecycleAdapter(this, this, tasksList, completeTasks);
        taskViewRecycle.setAdapter(tasksAdapter);
        taskViewRecycle.setLayoutManager(new LinearLayoutManager(this));

        scheduleAllTaskNotifications(this, tasksList);

        MaterialToolbar topAppBar = findViewById(R.id.appMenuBar);
        setSupportActionBar(topAppBar);

        fabAddTask.setOnClickListener(this::createTask);

        // select the "Ongoing" button on activity start
        filterButtonGroup.check(R.id.btnOngoing);

        btnCompleted.setOnClickListener(v -> tasksAdapter.showCompletedTasks());
        btnOngoing.setOnClickListener(v -> tasksAdapter.showOngoingTasks());
    }

    public void createTask(View view) {
        CreateTasksFragment taskDialog = new CreateTasksFragment();
        taskDialog.show(getSupportFragmentManager(), "AddTaskDialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveTasks(ArrayList<TaskList> taskLists) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskLists);
        Log.d(TAG, "Saving JSON: " + json);
        editor.putString(TASK_LIST_KEY, json);
        editor.apply();
    }

    private void saveCompleteTasks(ArrayList<CompleteTasks> completeTasks){
        SharedPreferences sharedPreferences =  getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(completeTasks);
        Log.d(TAG, "Saving complete tasks from adapter: " + json);
        editor.putString(COMPLETE_TASK_LIST_KEY, json);
        editor.apply();
    }

    private ArrayList<TaskList> loadTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(TASK_LIST_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<TaskList>>() {}.getType();
            try {
                ArrayList<TaskList> loadedTasks = gson.fromJson(json, type);
                if (loadedTasks != null){
                    // sort the list by date and time
                    Collections.sort(loadedTasks);
                    return loadedTasks;
                } else {
                    return new ArrayList<>();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading tasks from JSON", e);
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<CompleteTasks> loadCompleteTasks(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(COMPLETE_TASK_LIST_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<CompleteTasks>>() {}.getType();
            try {
                ArrayList<CompleteTasks> loadedTasks = gson.fromJson(json, type);

                return loadedTasks != null ? loadedTasks : new ArrayList<>();
            } catch (Exception e){
                Log.e(TAG, "Error lading tasks from JSON complete", e);
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    // --- TaskDialogListener Implementation ---
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onTaskAdded(TaskList task) {
        tasksList.add(task);
        // sort the list after added
        Collections.sort(tasksList);
        saveTasks(tasksList);
        // Update adapter's with sorted list and notify
        tasksAdapter.setTasks(tasksList);
        tasksAdapter.notifyDataSetChanged();
        Log.d(TAG, "Task Added: " + task.getTaskTitle());
        scheduleSingleTaskNotification(this, task, tasksList.size() - 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onTaskUpdated(TaskList task, int position) {
        if (position >= 0 && position < tasksList.size()) {
            cancelAlarm(this, position); // Cancel old alarm
            tasksList.set(position, task); // Update MainActivity's list
            Collections.sort(tasksList);
            saveTasks(tasksList); // Save the updated and sorted list

            tasksAdapter.setTasks(tasksList);
            // Update adapter's internal list and notify UI
            tasksAdapter.notifyDataSetChanged();

            // Find the new index of the updated task (since sorting may change its position)
            int newPosition = tasksList.indexOf(task);
            Log.d(TAG, "Task Updated at position " + position + ": " + task.getTaskTitle());
            scheduleSingleTaskNotification(this, task, newPosition); // Schedule updated task
        } else {
            Log.e(TAG, "Invalid position for task update: " + position);
        }
    }

    @Override
    public void onTaskDeleted(int editPosition) {
        if (editPosition != RecyclerView.NO_POSITION){
            cancelAlarm(this, editPosition);
            TaskList deletedTask = tasksList.remove(editPosition);
            saveTasks(tasksList);
            tasksAdapter.setTasks(tasksList); // update adapter internal list
            tasksAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Task '" + deletedTask.getTaskTitle() + "' deleted", Toast.LENGTH_SHORT).show();
            scheduleAllTaskNotifications(this, tasksList); // reschedule all tasks
        } else {
            Log.e(TAG, "Invalid position for task deletion: " + editPosition);
        }
    }

    // --- Notification Permission Handling ---
    private void requestNotificationPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Notification permission already granted.");
            checkExactAlarmPermission();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            // Consider showing a dialog explaining why the permission is needed
            Toast.makeText(this, "Please grant notification permission for reminders.", Toast.LENGTH_LONG).show();
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void checkExactAlarmPermission() {
        if (alarmManager == null)
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Exact alarm permission not granted. Requesting...");
            Toast.makeText(this, "App needs permission to schedule exact alarms for reminders.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            try {
                requestExactAlarmLauncher.launch(intent);
            } catch (Exception e) {
                Log.e(TAG, "Could not launch request exact alarm settings", e);
                Toast.makeText(this, "Could not open exact alarm settings.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Exact alarm permission already granted.");
        }
    }

    // --- Create Notification Channel ---
    private void createNotificationChannel() {
        // Use hardcoded strings
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
        channel.setDescription(NOTIFICATION_CHANNEL_DESC);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created.");
        }
    }

    // --- Alarm Scheduling & Cancellation ---
    public void scheduleSingleTaskNotification(Context context, TaskList task, int requestCode) {
        // ... (Implementation remains the same as previous response)
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null, cannot schedule task.");
            return;
        }

        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Cannot schedule exact alarm: permission denied for task: " + task.getTaskTitle());
            return;
        }

        String dateTimeString = task.getTaskDateString() + " " + task.getTaskTimeString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        try {
            Date date = sdf.parse(dateTimeString);
            if (date == null) {
                Log.e(TAG, "Parsed date is null for task: " + task.getTaskTitle());
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            long triggerTimeMillis = calendar.getTimeInMillis();

            if (triggerTimeMillis < System.currentTimeMillis()) {
                Log.w(TAG, "Task time is in the past, not scheduling: " + task.getTaskTitle());
                return;
            }

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra(NotificationReceiver.EXTRA_TASK_TITLE, task.getTaskTitle());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
            );

            Log.d(TAG, "Scheduled notification for task: " + task.getTaskTitle() + " at " + sdf.format(date) + " with reqCode: " + requestCode);

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date/time for task: " + task.getTaskTitle() + ", DateTime: " + dateTimeString, e);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification for task: " + task.getTaskTitle(), e);
        }
    }

    public void scheduleAllTaskNotifications(Context context, List<TaskList> taskList) {
        // ... (Implementation remains the same as previous response)
        Log.d(TAG, "Scheduling all " + taskList.size() + " tasks.");
        for (int i = 0; i < taskList.size(); i++) {
            scheduleSingleTaskNotification(context, taskList.get(i), i);
        }
    }

    public void cancelAlarm(Context context, int requestCode) {
        // ... (Implementation remains the same as previous response)
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null, cannot cancel alarm.");
            return;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.d(TAG, "Cancelled alarm with request code: " + requestCode);
    }
}