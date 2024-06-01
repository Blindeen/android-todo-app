package com.project.todolist;

import static com.project.todolist.Utils.displayToast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.todolist.activity.AddEditTaskActivity;
import com.project.todolist.activity.SettingsActivity;
import com.project.todolist.db.AppDatabase;
import com.project.todolist.db.dao.CategoryDao;
import com.project.todolist.db.entity.Category;
import com.project.todolist.interfaces.ResponseHandler;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final static boolean HIDE_DONE_TASKS_DEFAULT = false;
    private final static Long CHOSEN_CATEGORY_ID_DEFAULT = null;

    protected AppDatabase database;
    protected SharedPreferences sharedPreferences;
    protected boolean hideDoneTasks;
    protected Long chosenCategory;
    protected Disposable categoryListQuerySubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.relative_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        database = AppDatabase.getDatabase(this);
        initializeSharedPreferences();
        loadAppPreferences();
    }

    protected void initializeSharedPreferences() {
        String sharedPreferencesFilename = getString(R.string.shared_pref_filename);
        sharedPreferences = getSharedPreferences(sharedPreferencesFilename, MODE_PRIVATE);
    }

    protected void loadAppPreferences() {
        hideDoneTasks = sharedPreferences.getBoolean(getString(R.string.hide_done_tasks_key), HIDE_DONE_TASKS_DEFAULT);
        chosenCategory = sharedPreferences.getLong(getString(R.string.chosen_category_key), -1);
        if (chosenCategory == -1) {
            chosenCategory = CHOSEN_CATEGORY_ID_DEFAULT;
        }
    }

    public void addTaskButtonOnClick(View view) {
        openNewActivity(AddEditTaskActivity.class);
    }

    public void settingsButtonOnClick(View view) {
        openNewActivity(SettingsActivity.class);
    }

    private <T> void openNewActivity(Class<T> className) {
        Intent intent = new Intent(this, className);
        startActivity(intent);
    }

    protected void fetchCategories(ResponseHandler responseHandler) {
        CategoryDao categoryDao = database.categoryDao();
        Single<List<Category>> categoryListSingle = categoryDao.getAll();
        categoryListQuerySubscriber = categoryListSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        responseHandler::handle,
                        throwable -> displayToast(this, "Error: It was not possible to fetch categories")
                );
    }
}