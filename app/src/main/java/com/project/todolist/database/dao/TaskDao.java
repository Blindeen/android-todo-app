package com.project.todolist.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Update;

import com.project.todolist.database.entity.Task;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM Task " +
            "WHERE (isDone = false OR isDone != :hideDone) " +
            "AND title LIKE :titlePattern " +
            "AND (categoryId = :categoryId OR :categoryId IS NULL) " +
            "ORDER BY isDone, doneAt"
    )
    Single<List<Task>> getTasks(String titlePattern, boolean hideDone, Long categoryId);

    @Query(
            "INSERT INTO Task (title, description, doneAt, categoryId, notification)" +
                    "VALUES (:title, :description, :dateTime, :categoryId, :notification)"
    )
    Completable insertTask(String title, String description, String dateTime, long categoryId, boolean notification);

    @Update
    Completable updateTask(Task task);

    @Delete
    Completable deleteTask(Task task);
}
