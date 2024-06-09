package com.project.todolist.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

public class TaskWithAttachments implements Serializable {
    @Embedded
    private Task task;

    @Relation(
            parentColumn = "taskId",
            entityColumn = "taskId"
    )
    private List<Attachment> attachments;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
