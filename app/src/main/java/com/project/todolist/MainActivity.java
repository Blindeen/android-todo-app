package com.project.todolist;

import static com.project.todolist.notification.NotificationUtils.createNotificationChannel;

import android.content.Intent;
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
    protected DatabaseManager databaseManager;
    private AppPreferences appPreferences;

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
        appPreferences = new AppPreferences(this);
        createNotificationChannel(this);
        configSearchBar();
        configTaskRecycleViewer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appPreferences.loadAppPreferences();
        taskListQuerySubscriber = databaseManager.fetchTasks(
                titlePattern,
                appPreferences.isHideDoneTasks(),
                appPreferences.getChosenCategory(),
                this::setTaskRecyclerViewData
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Disposable[] disposables = {categoryListQuerySubscriber, taskListQuerySubscriber};
        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
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
                        appPreferences.isHideDoneTasks(),
                        appPreferences.getChosenCategory(),
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