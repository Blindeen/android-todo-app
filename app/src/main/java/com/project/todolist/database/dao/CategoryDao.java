package com.project.todolist.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.todolist.database.entity.Category;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY category.name")
    Single<List<Category>> getAll();

    @Insert
    void insertCategories(Category... category);
}
