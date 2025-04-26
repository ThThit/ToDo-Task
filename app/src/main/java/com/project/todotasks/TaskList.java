package com.project.todotasks;

public class TaskList {
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
}