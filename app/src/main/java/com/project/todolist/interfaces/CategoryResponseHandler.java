package com.project.todolist.interfaces;

import com.project.todolist.database.entity.Category;

import java.util.List;

@FunctionalInterface
public interface CategoryResponseHandler {
    void onCategoriesFetched(List<Category> categories);
}
