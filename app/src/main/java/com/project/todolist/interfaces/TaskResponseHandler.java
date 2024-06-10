package com.project.todolist.interfaces;

import com.project.todolist.database.entity.TaskWithAttachments;

import java.util.List;

@FunctionalInterface
public interface TaskResponseHandler {
    void onTasksFetched(List<TaskWithAttachments> taskWithAttachments);
}
