package com.project.todotasks;
import java.sql.Time;

public class TaskList {
    private String taskTitle;
    private String taskTime;
    private String taskDate;


    public TaskList(String taskTitle, String taskTime, String taskDate) {
        this.taskTitle = taskTitle;
        this.taskTime = taskTime;
        this.taskDate = taskDate;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public String getTaskDate() {
        return taskDate;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime;
    }

    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
    }

    @Override
    public String toString() {
        return "TaskList{" +
                "taskTitle='" + taskTitle + '\'' +
                ", taskTime='" + taskTime + '\'' +
                ", taskDate='" + taskDate + '\'' +
                '}';
    }
}
