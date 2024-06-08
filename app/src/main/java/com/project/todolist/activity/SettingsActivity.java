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
import com.project.todolist.spinner.NotificationTimeSpinnerItem;
import com.project.todolist.R;
import com.project.todolist.database.entity.Category;

import static com.project.todolist.Utils.*;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends MainActivity {
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

    private void configureForm() {
        configureHideDoneTasksCheckbox();
        fetchCategories(this::setCategorySpinnerData);
        configureNotificationTimeSpinner();
    }

    private void configureHideDoneTasksCheckbox() {
        CheckBox hideDoneTasksCheckbox = findViewById(R.id.checkbox_hide_done_tasks);
        hideDoneTasksCheckbox.setChecked(hideDoneTasks);
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

        int selectedItem = spinnerData.indexOf(new NotificationTimeSpinnerItem(null, notificationBeforeCompletionMs));
        notificationTimeSpinner.setSelection(selectedItem);
    }

    private ArrayList<NotificationTimeSpinnerItem> prepareNotificationTimeSpinnerData() {
        ArrayList<NotificationTimeSpinnerItem> spinnerItems = new ArrayList<>();
        spinnerItems.add(new NotificationTimeSpinnerItem("0 min", 0));
        spinnerItems.add(new NotificationTimeSpinnerItem("5 min", 300000));
        spinnerItems.add(new NotificationTimeSpinnerItem("30 min", 1800000));
        spinnerItems.add(new NotificationTimeSpinnerItem("1 h", 3600000));
        spinnerItems.add(new NotificationTimeSpinnerItem("1 day", 86400000));

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
        notificationBeforeCompletionMs = item.getValue();
    }

    private void saveAppPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.hide_done_tasks_key), hideDoneTasks);
        editor.putLong(getString(R.string.notification_before_min_key), notificationBeforeCompletionMs);
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