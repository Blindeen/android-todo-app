package com.project.todolist.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Task implements Serializable {
    public Task() {
    }

    public Task(String title, String description, String doneAt, boolean notification, long categoryId) {
        this.title = title;
        this.description = description;
        this.doneAt = doneAt;
        this.notification = notification;
        this.categoryId = categoryId;
    }

    @PrimaryKey(autoGenerate = true)
    private long taskId;
    private String title;
    private String description;
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    private String createdAt;
    @ColumnInfo(defaultValue = "0")
    private boolean isDone;
    private String doneAt;
    private boolean notification;
    private long categoryId;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(String doneAt) {
        this.doneAt = doneAt;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
