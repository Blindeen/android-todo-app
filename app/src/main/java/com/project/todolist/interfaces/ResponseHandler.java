package com.project.todolist.interfaces;

import com.project.todolist.db.entity.Category;

import java.util.List;

@FunctionalInterface
public interface ResponseHandler {
    void handle(List<Category> categories);
}
