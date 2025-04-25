package com.project.todotasks;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CreateTasksFragment.TaskDialogListener {

    private ArrayList<TaskList> tasksList = new ArrayList<>();
    private TasksRecycleAdapter tasksAdapter;

    private final String CHANNEL_ID = "task_status";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

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

        // for notification
        requestNotificationPermission();
        createNotificationChannel();

        RecyclerView taskViewRecycle = findViewById(R.id.taskView);

        // load from shared preferences
        tasksList = loadTasks();

        // view adapter
        tasksAdapter = new TasksRecycleAdapter(this, this);
        tasksAdapter.setTasks(tasksList);

        MaterialToolbar topAppBar = findViewById(R.id.appMenuBar);
        setSupportActionBar(topAppBar);

        taskViewRecycle.setAdapter(tasksAdapter);

        taskViewRecycle.setLayoutManager(new LinearLayoutManager(this));

        // menu bar
        topAppBar.setNavigationOnClickListener(view ->
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        );
    }
    
    public void createTask(View view){
        CreateTasksFragment taskDialog = new CreateTasksFragment();
        taskDialog.show(getSupportFragmentManager(), "Add Task");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile){
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveTasks(ArrayList<TaskList> taskLists){
        SharedPreferences sharedPreferences = getSharedPreferences("My Tasks", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskLists); // convert to json
        Log.d("saveTasks", "Saving JSON: " + json);

        editor.putString("task_list", json);
        editor.apply();
    }


    // load from json
    private ArrayList<TaskList> loadTasks(){
        SharedPreferences sharedPreferences = getSharedPreferences("My Tasks", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("task_list", null);

        if (json != null){
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<TaskList>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void onTaskAdded(TaskList tasks) {
        tasksList.add(tasks);
        saveTasks(tasksList);
        tasksAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskUpdated(TaskList task, int position) {
        ArrayList<TaskList> currentTasks = loadTasks();
        // update the selected task
        if (position >0 && position < currentTasks.size()){
            currentTasks.set(position, task);

            // update to share preferences
            saveTasks(currentTasks);
            tasksAdapter.updateTask(task, position);
        }
    }

    // request notification permission
    private void requestNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}
                        ,REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    // handle request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                openAppNotificationSettings();
            }
        }
    }

    // open notification setting directly
    private void openAppNotificationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    // notifications
    private void createNotificationChannel(){
        NotificationChannel taskReminderChannel = new NotificationChannel(
                CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        // channel initial settings
        taskReminderChannel.setLightColor(Color.GREEN);
        taskReminderChannel.setDescription("Task Reminder");

        // submit to notification manager
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(taskReminderChannel);
    }

    // display notification

}