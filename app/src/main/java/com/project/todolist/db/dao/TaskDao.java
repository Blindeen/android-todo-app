package com.project.todolist.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Update;

import com.project.todolist.db.entity.Task;
import com.project.todolist.db.entity.TaskWithCategory;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM Task WHERE title LIKE :titlePattern ORDER BY isDone, doneAt")
    Single<List<TaskWithCategory>> getTasks(String titlePattern);

    @Query(
            "INSERT INTO Task (title, description, doneAt, categoryId, notification)" +
                    "VALUES (:title, :description, :dateTime, :categoryId, :notification)"
    )
    Completable insertTask(String title, String description, String dateTime, long categoryId, boolean notification);

    @Update
    Completable updateTask(Task task);

    @Delete
    void delete(Task task);
}
