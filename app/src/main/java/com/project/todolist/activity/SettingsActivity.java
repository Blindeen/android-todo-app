package com.project.todolist.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.todolist.MainActivity;
import com.project.todolist.NotificationTimeSpinnerItem;
import com.project.todolist.R;
import com.project.todolist.db.AppDatabase;
import com.project.todolist.db.dao.CategoryDao;
import com.project.todolist.db.entity.Category;

import static com.project.todolist.Utils.*;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsActivity extends MainActivity {
    private static final Integer NOTIFICATION_BEFORE_COMPLETION_MIN_DEFAULT = 0;

    private Integer notificationBeforeCompletionMin;
    private Disposable categoryListQuerySubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
        configureForm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (categoryListQuerySubscriber != null) {
            categoryListQuerySubscriber.dispose();
        }
    }

    @Override
    protected void loadAppPreferences() {
        super.loadAppPreferences();
        notificationBeforeCompletionMin = sharedPreferences.getInt(
                getString(R.string.notification_before_min_key),
                NOTIFICATION_BEFORE_COMPLETION_MIN_DEFAULT
        );
    }

    private void configureForm() {
        configureHideDoneTasksCheckbox();
        configureCategorySpinner();
        configureNotificationTimeSpinner();
    }

    private void configureHideDoneTasksCheckbox() {
        CheckBox hideDoneTasksCheckbox = findViewById(R.id.checkbox_hide_done_tasks);
        hideDoneTasksCheckbox.setChecked(hideDoneTasks);
    }

    private void configureCategorySpinner() {
        CategoryDao categoryDao = database.categoryDao();
        Single<List<Category>> categoryListSingle = categoryDao.getAll();
        categoryListQuerySubscriber = categoryListSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::setCategorySpinnerData,
                        throwable -> displayToast(this, "Error: It was not possible to fetch categories")
                );
    }

    private void setCategorySpinnerData(List<Category> categoryList) {
        Spinner categorySpinner = findViewById(R.id.spinner_visible_category);

        categoryList.add(0, new Category(0, ""));
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        if (chosenCategory != null) {
            int selectedItem = categoryList.indexOf(new Category(chosenCategory, ""));
            categorySpinner.setSelection(selectedItem);
        }
    }

    private void configureNotificationTimeSpinner() {
        Spinner notificationTimeSpinner = findViewById(R.id.spinner_notification_time);

        ArrayList<NotificationTimeSpinnerItem> spinnerData = prepareNotificationTimeSpinnerData();
        ArrayAdapter<NotificationTimeSpinnerItem> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notificationTimeSpinner.setAdapter(adapter);

        int selectedItem = spinnerData.indexOf(new NotificationTimeSpinnerItem(null, notificationBeforeCompletionMin));
        notificationTimeSpinner.setSelection(selectedItem);
    }

    private ArrayList<NotificationTimeSpinnerItem> prepareNotificationTimeSpinnerData() {
        ArrayList<NotificationTimeSpinnerItem> spinnerItems = new ArrayList<>();
        spinnerItems.add(new NotificationTimeSpinnerItem("0 min", 0));
        spinnerItems.add(new NotificationTimeSpinnerItem("5 min", 5));
        spinnerItems.add(new NotificationTimeSpinnerItem("30 min", 30));
        spinnerItems.add(new NotificationTimeSpinnerItem("1 h", 60));
        spinnerItems.add(new NotificationTimeSpinnerItem("1 day", 1440));

        return spinnerItems;
    }

    private void handleSettingsForm() {
        handleHideDoneTasksCheckbox();
        handleChosenCategorySpinner();
        handleNotificationTimeSpinner();
    }

    private void handleHideDoneTasksCheckbox() {
        CheckBox hideDoneTasksCheckbox = findViewById(R.id.checkbox_hide_done_tasks);
        hideDoneTasks = hideDoneTasksCheckbox.isChecked();
    }

    private void handleChosenCategorySpinner() {
        Spinner chosenCategorySpinner = findViewById(R.id.spinner_visible_category);
        Category chosenCategory = (Category) chosenCategorySpinner.getSelectedItem();
        if (chosenCategory != null) {
            this.chosenCategory = chosenCategory.getCategoryId();
        }
    }

    private void handleNotificationTimeSpinner() {
        Spinner notificationTimeSpinner = findViewById(R.id.spinner_notification_time);
        NotificationTimeSpinnerItem item = (NotificationTimeSpinnerItem) notificationTimeSpinner.getSelectedItem();
        notificationBeforeCompletionMin = item.getValue();
    }

    private void saveAppPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.hide_done_tasks_key), hideDoneTasks);
        editor.putInt(getString(R.string.notification_before_min_key), notificationBeforeCompletionMin);
        editor.putLong(getString(R.string.chosen_category_key), chosenCategory);
        editor.apply();
    }

    public void applyButtonOnClick(View view) {
        handleSettingsForm();
        saveAppPreferences();
        displayToast(this, "Settings applied successfully");
        finish();
    }
}