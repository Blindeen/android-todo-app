package com.project.todolist.interfaces;

@FunctionalInterface
public interface DeleteTaskResponseHandler {
    void onTaskDeleted();
}
