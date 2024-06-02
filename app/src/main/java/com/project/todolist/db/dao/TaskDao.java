package com.project.todolist.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.project.todolist.db.entity.Task;
import com.project.todolist.db.entity.TaskWithCategory;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM Task")
    Single<List<TaskWithCategory>> getAll();

    @Insert
    Completable insertTasks(Task... tasks);

    @Update
    void updateTasks(Task... tasks);

    @Delete
    void delete(Task task);
}
