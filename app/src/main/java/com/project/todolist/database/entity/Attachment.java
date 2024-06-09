package com.project.todolist.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Attachment implements Serializable {
    public Attachment() {
    }

    public Attachment(String name, String path, Long taskId) {
        this.name = name;
        this.path = path;
        this.taskId = taskId;
    }

    @PrimaryKey(autoGenerate = true)
    private long attachmentId;
    private String name;
    private String path;
    private Long taskId;

    public long getAttachmentId() {
        return attachmentId;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setAttachmentId(long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
