package com.project.todotasks;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TaskList implements Comparable<TaskList>{
    private String taskTitle;
    private String taskDateString;
    private String taskTimeString;
    // Optional: Add a unique ID for more robust alarm handling later
    // private long id;
    public TaskList(String taskTitle, String taskDateString, String taskTimeString) {
        this.taskTitle = taskTitle;
        this.taskDateString = taskDateString;
        this.taskTimeString = taskTimeString;
        // this.id = System.currentTimeMillis(); // Assign unique ID if implementing that feature
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskDateString() {
        return taskDateString;
    }
    public String getTaskTimeString() {
        return taskTimeString;
    }


    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public void setTaskDateString(String taskDateString) {
        this.taskDateString = taskDateString;
    }

    public void setTaskTimeString(String taskTimeString) {
        this.taskTimeString = taskTimeString;
    }

    @Override
    public String toString() {
        // Updated toString to use correct field names
        return "TaskList{" +
                "taskTitle='" + taskTitle + '\'' +
                ", taskDateString='" + taskDateString + '\'' +
                ", taskTimeString='" + taskTimeString + '\'' +
                // ", id=" + id + // Include ID if implementing
                '}';
    }

    @Override
    public int compareTo(TaskList otherTask) {
        Objects.requireNonNull(otherTask, "Cannot compare to a null");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date thisDateTime = sdf.parse(this.taskDateString + " " + this.taskTimeString);
            Date otherDateTime = sdf.parse(otherTask.taskDateString + " " + otherTask.taskTimeString);
            // hande null data from parse
            if (thisDateTime == null && otherDateTime == null) return 0;
            if (thisDateTime == null) return -1;
            if (otherDateTime == null) return  1;

            return thisDateTime.compareTo(otherDateTime);
        } catch (ParseException e){
            e.printStackTrace();
            return 0;
        }
    }
}