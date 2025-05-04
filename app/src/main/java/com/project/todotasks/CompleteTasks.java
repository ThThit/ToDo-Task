package com.project.todotasks;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CompleteTasks {
    private String taskTitle;
    private String taskTime;
    private String taskDate;
    private LocalDateTime finishTime;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @NonNull
    @Override
    public String toString() {
        return "CompleteTasks{" +
                "taskTitle='" + taskTitle + '\'' +
                ", taskTime='" + taskTime + '\'' +
                ", taskDate='" + taskDate + '\'' +
                ", finishTime=" + finishTime +
                '}';
    }

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

    public String getFinishDateString() {
        if (finishTime != null) {
            return finishTime.format(DATE_FORMATTER);
        } else {
            return ""; // Or return null, or a default string
        }
    }
}
