package com.project.todotasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import kotlinx.coroutines.scheduling.Task;

public class TasksRecycleAdapter extends RecyclerView.Adapter<TasksRecycleAdapter.ViewHolder>  {

    private ArrayList<TaskList> tasks = new ArrayList<>();
    // Fragment Activity
    private FragmentActivity activity;
    private final Context context;

    public TasksRecycleAdapter(Context context, FragmentActivity activity) {
        this.context = context;
        this.activity = activity;
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
        holder.taskDate.setText(taskList.getTaskDate());
        holder.taskTime.setText(taskList.getTaskTime());

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION){
                    // edit task
                    TaskList task = tasks.get(currentPosition);
                    CreateTasksFragment taskEdit = new CreateTasksFragment();
                    taskEdit.setTaskToEdit(task, currentPosition); // pass task and index
                    if (activity instanceof CreateTasksFragment.TaskDialogListener) {
                        taskEdit.setListener((CreateTasksFragment.TaskDialogListener) activity);
                    }
                    taskEdit.show(activity.getSupportFragmentManager(), "Edit Task");
                    Toast.makeText(context, taskList.getTaskTitle().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        holder.btnTaskDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION){
                    // get from the current list
                    ArrayList<TaskList> updateTaskList = loadTasks();
                    // remove task
                    if (currentPosition < updateTaskList.size()) {
                        updateTaskList.remove(currentPosition);
                        // save the list again
                        saveTasks(updateTaskList);
                        // update view
                        tasks.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, tasks.size());

                    }
                }
                Toast.makeText(context, "Task Done", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private ArrayList<TaskList> loadTasks() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("My Tasks", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("task_list", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<TaskList>> () {}
                    .getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private void saveTasks(ArrayList<TaskList> taskLists){
        SharedPreferences sharedPreferences = context.getSharedPreferences("My Tasks", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskLists); // convert to json
        Log.d("saveTasks", "Saving JSON: " + json);

        editor.putString("task_list", json);
        editor.apply();
    }

    public void setTasks(ArrayList<TaskList> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged(); // change data
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView taskTitle, taskTime, taskDate;
        View parent;
        FloatingActionButton btnTaskDelete, btnTaskDone;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            parent = itemView;
            taskTitle = itemView.findViewById(R.id.task);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskDate = itemView.findViewById(R.id.taskDate);
            btnTaskDone = itemView.findViewById(R.id.btnTaskDone);
        }
    }

    public void updateTask(TaskList updateTask, int position){
        tasks.set(position, updateTask);
        notifyItemChanged(position);
    }
}
