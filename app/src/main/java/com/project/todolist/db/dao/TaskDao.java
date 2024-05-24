package com.project.todolist.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.project.todolist.db.entity.Task;
import com.project.todolist.db.entity.TaskWithCategory;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM Task")
    List<TaskWithCategory> getAll();

    @Insert
    void insertTasks(Task... tasks);

    @Update
    void updateTasks(Task... tasks);

    @Delete
    void delete(Task task);
}
