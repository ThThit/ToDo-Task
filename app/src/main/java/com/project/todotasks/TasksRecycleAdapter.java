package com.project.todotasks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TasksRecycleAdapter extends RecyclerView.Adapter<TasksRecycleAdapter.ViewHolder> {

    private ArrayList<TaskList> tasks = new ArrayList<>();

    public TasksRecycleAdapter(Context context) {
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

//        holder.parent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int currentPosition = holder.getAdapterPosition(); // get position
//                if (currentPosition != RecyclerView.NO_POSITION){
//                    Toast.makeText(context, tasks.get(currentPosition).getTaskTitle() + " Selected", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(ArrayList<TaskList> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged(); // change data
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView taskTitle, taskTime, taskDate;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            taskTitle = itemView.findViewById(R.id.task);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskDate = itemView.findViewById(R.id.taskDate);
            itemView.findViewById(R.id.taskList);
        }
    }

}
