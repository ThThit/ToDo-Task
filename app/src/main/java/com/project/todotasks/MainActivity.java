package com.project.todotasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CreateTasksDialogFragment.TaskDialogListener {

    private ArrayList<TaskList> tasksList = new ArrayList<>();
    private TasksRecycleAdapter tasksAdapter;

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

        RecyclerView taskViewRecycle = findViewById(R.id.taskView);
        FloatingActionButton btnAddTask = findViewById(R.id.btnNewTask);

        // load from shared preferences
        tasksList = loadTasks();

        // view adapter
        tasksAdapter = new TasksRecycleAdapter(this);
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
        CreateTasksDialogFragment taskDialog = new CreateTasksDialogFragment();
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
}