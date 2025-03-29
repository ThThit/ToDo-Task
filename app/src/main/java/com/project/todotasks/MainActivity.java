package com.project.todotasks;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


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

        ArrayList<TaskList> tasks = new ArrayList<>();
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));
        tasks.add(new TaskList("Finish Laundry", "10:30", "12/04/2025"));

        // view adapter
        TasksRecycleAdapter tasksRecycleAdapter = new TasksRecycleAdapter(this);
        tasksRecycleAdapter.setTasks(tasks);

        MaterialToolbar topAppBar = findViewById(R.id.appMenuBar);
        setSupportActionBar(topAppBar);

        taskViewRecycle.setAdapter(tasksRecycleAdapter);

        taskViewRecycle.setLayoutManager(new LinearLayoutManager(this));

        // menu bar
        topAppBar.setNavigationOnClickListener(view ->
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        );
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
}