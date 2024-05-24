package com.project.todolist;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.project.todolist.db.AppDatabase;
import com.project.todolist.db.dao.CategoryDao;
import com.project.todolist.db.dao.TaskDao;
import com.project.todolist.db.entity.Category;

import java.io.IOException;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private AppDatabase appDatabase;
    private TaskDao taskDao;
    private CategoryDao categoryDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        taskDao = appDatabase.taskDao();
        categoryDao = appDatabase.categoryDao();
    }

    @After
    public void closeDb() {
        appDatabase.close();
    }

    @Test
    public void saveCategoriesTest() {
        Category category = new Category();
        category.setName("Test Category");
        categoryDao.insertCategories(category);
        Single<List<Category>> categories = categoryDao.getAll();
        System.out.println(categories);
    }
}