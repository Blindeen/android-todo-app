package com.project.todolist;

import static com.project.todolist.notification.NotificationUtils.createNotificationChannel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.todolist.activity.AddEditTaskActivity;
import com.project.todolist.activity.SettingsActivity;
import com.project.todolist.adapter.TaskListAdapter;
import com.project.todolist.database.DatabaseManager;
import com.project.todolist.database.entity.TaskWithAttachments;

import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    private final static boolean HIDE_DONE_TASKS_DEFAULT = false;
    private final static Long CHOSEN_CATEGORY_ID_DEFAULT = null;
    private static final Integer NOTIFICATION_BEFORE_COMPLETION_MIN_DEFAULT = 0;

    protected DatabaseManager databaseManager;
    protected SharedPreferences sharedPreferences;
    protected boolean hideDoneTasks;
    protected Long chosenCategory;
    protected Long notificationBeforeCompletionMs;

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

        databaseManager = new DatabaseManager(this);
        createNotificationChannel(this);
        initializeSharedPreferences();
        configSearchBar();
        configTaskRecycleViewer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppPreferences();
        taskListQuerySubscriber = databaseManager.fetchTasks(
                titlePattern,
                hideDoneTasks,
                chosenCategory,
                this::setTaskRecyclerViewData
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (taskListQuerySubscriber != null && !taskListQuerySubscriber.isDisposed()) {
            taskListQuerySubscriber.dispose();
        }
    }

    private void configSearchBar() {
        TextView searchInput = findViewById(R.id.input_search);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String searchInputValue = s.toString();
                titlePattern = "%" + searchInputValue + "%";
                taskListQuerySubscriber = databaseManager.fetchTasks(
                        titlePattern,
                        hideDoneTasks,
                        chosenCategory,
                        MainActivity.this::setTaskRecyclerViewData
                );
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void configTaskRecycleViewer() {
        taskListView = findViewById(R.id.recycler_task_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        taskListView.setLayoutManager(linearLayoutManager);
    }

    protected void initializeSharedPreferences() {
        String sharedPreferencesFilename = getString(R.string.shared_pref_filename);
        sharedPreferences = getSharedPreferences(sharedPreferencesFilename, MODE_PRIVATE);
    }

    protected void loadAppPreferences() {
        hideDoneTasks = sharedPreferences.getBoolean(getString(R.string.hide_done_tasks_key), HIDE_DONE_TASKS_DEFAULT);
        chosenCategory = sharedPreferences.getLong(getString(R.string.chosen_category_key), 0);
        if (chosenCategory == 0) {
            chosenCategory = CHOSEN_CATEGORY_ID_DEFAULT;
        }
        notificationBeforeCompletionMs = sharedPreferences.getLong(
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

    private void setTaskRecyclerViewData(List<TaskWithAttachments> taskList) {
        TaskListAdapter taskListAdapter = (TaskListAdapter) taskListView.getAdapter();
        if (taskListAdapter == null) {
            taskListAdapter = new TaskListAdapter(this, taskList);
            taskListView.setAdapter(taskListAdapter);
        } else {
            taskListAdapter.setData(taskList);
        }
    }
}