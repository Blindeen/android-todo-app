package com.project.todolist.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Attachment {
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

    public long getTaskId() {
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

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
