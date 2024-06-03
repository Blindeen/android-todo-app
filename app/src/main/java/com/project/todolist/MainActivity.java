package com.project.todolist;

import static com.project.todolist.Utils.displayToast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.todolist.activity.AddEditTaskActivity;
import com.project.todolist.activity.SettingsActivity;
import com.project.todolist.adapter.TaskListAdapter;
import com.project.todolist.db.AppDatabase;
import com.project.todolist.db.dao.CategoryDao;
import com.project.todolist.db.dao.TaskDao;
import com.project.todolist.db.entity.Category;
import com.project.todolist.db.entity.TaskWithCategory;
import com.project.todolist.interfaces.ResponseHandler;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.project.todolist.Utils.*;

import androidx.recyclerview.widget.LinearLayoutManager;

public class MainActivity extends AppCompatActivity {
    private final static boolean HIDE_DONE_TASKS_DEFAULT = false;
    private final static Long CHOSEN_CATEGORY_ID_DEFAULT = null;
    private static final Integer NOTIFICATION_BEFORE_COMPLETION_MIN_DEFAULT = 0;

    protected AppDatabase database;
    protected SharedPreferences sharedPreferences;
    protected boolean hideDoneTasks;
    protected Long chosenCategory;
    protected Integer notificationBeforeCompletionMin;

    protected Disposable categoryListQuerySubscriber;
    private Disposable taskListQuerySubscriber;

    private String titlePattern = "%%";
    private RecyclerView taskListView;

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

        taskListView = findViewById(R.id.recycler_task_list);

        createNotificationChannel(this);
        database = AppDatabase.getDatabase(this);
        initializeSharedPreferences();
        loadAppPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTasks(titlePattern);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (taskListQuerySubscriber != null) {
            taskListQuerySubscriber.dispose();
        }
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
        notificationBeforeCompletionMin = sharedPreferences.getInt(
                getString(R.string.notification_before_min_key),
                NOTIFICATION_BEFORE_COMPLETION_MIN_DEFAULT
        );
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

    private void fetchTasks(String titlePattern) {
        TaskDao taskDao = database.taskDao();
        Single<List<TaskWithCategory>> taskList = taskDao.getTasks(titlePattern);
        taskListQuerySubscriber = taskList
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::setTaskRecyclerData,
                        throwable -> displayToast(this, "Unable to fetch tasks")
                );
    }

    private void setTaskRecyclerData(List<TaskWithCategory> taskList) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        taskListView.setLayoutManager(linearLayoutManager);

        TaskListAdapter taskListAdapter = new TaskListAdapter(taskList);
        taskListView.setAdapter(taskListAdapter);
    }

    public void searchButtonOnClick(View view) {
        TextView searchInput = findViewById(R.id.input_search);
        String searchInputValue = searchInput.getText().toString();
        titlePattern = "%" + searchInputValue + "%";
        fetchTasks(titlePattern);
    }
}