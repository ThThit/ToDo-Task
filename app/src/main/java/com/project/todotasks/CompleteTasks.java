package com.project.todotasks;

import java.time.LocalDateTime;

public class CompleteTasks {
    private String taskTitle;
    private String taskTime;
    private String taskDate;
    private LocalDateTime finishTime;

    public CompleteTasks(String taskTitle, String taskTime, String taskDate, LocalDateTime finishTime) {
        this.taskTitle = taskTitle;
        this.taskTime = taskTime;
        this.taskDate = taskDate;
        this.finishTime = finishTime;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime;
    }

    public String getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }
}
