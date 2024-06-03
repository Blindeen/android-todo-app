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
    @Query("SELECT * FROM Task ORDER BY isDone")
    Single<List<TaskWithCategory>> getAll();

    @Query(
            "INSERT INTO Task (title, description, doneAt, categoryId, notification)" +
                    "VALUES (:title, :description, :dateTime, :categoryId, :notification)"
    )
    Completable insertTask(String title, String description, String dateTime, long categoryId, boolean notification);

    @Update
    void updateTask(Task task);

    @Delete
    void delete(Task task);
}
