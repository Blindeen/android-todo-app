package com.project.todolist.database;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.project.todolist.database.dao.AttachmentDao;
import com.project.todolist.database.dao.CategoryDao;
import com.project.todolist.database.dao.TaskDao;
import com.project.todolist.database.entity.Attachment;
import com.project.todolist.database.entity.Category;
import com.project.todolist.database.entity.Task;

@Database(
        version = 4,
        entities = {Task.class, Category.class, Attachment.class},
        autoMigrations = {
                @AutoMigration(from = 3, to = 4)
        }
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract CategoryDao categoryDao();
    public abstract AttachmentDao attachmentDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "database")
                            .createFromAsset("database/prepopulated_db.db")
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
