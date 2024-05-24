package com.project.todolist.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TaskWithCategory {
    @Embedded
    private Task task;

    @Relation(
            parentColumn = "categoryId",
            entityColumn = "categoryId"
    )
    private Category category;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
