package com.project.todolist.activity;

import static com.project.todolist.utils.Utils.displayToast;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.todolist.AppPreferences;
import com.project.todolist.R;
import com.project.todolist.database.DatabaseManager;
import com.project.todolist.database.entity.Category;
import com.project.todolist.spinner.NotificationTimeSpinnerItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private AppPreferences appPreferences;
    private Disposable categoryListQuerySubscriber;

    private CheckBox cbHideDoneTasks;
    private Spinner sCategory;
    private Spinner sNotificationTime;

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

        databaseManager = new DatabaseManager(this);
        appPreferences = new AppPreferences(this);

        initializeWidgets();
        configureForm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (categoryListQuerySubscriber != null && !categoryListQuerySubscriber.isDisposed()) {
            categoryListQuerySubscriber.dispose();
        }
    }

    private void initializeWidgets() {
        cbHideDoneTasks = findViewById(R.id.checkbox_hide_done_tasks);
        sCategory = findViewById(R.id.spinner_visible_category);
        sNotificationTime = findViewById(R.id.spinner_notification_time);
    }

    private void configureForm() {
        cbHideDoneTasks.setChecked(appPreferences.isHideDoneTasks());
        categoryListQuerySubscriber = databaseManager.fetchCategories(this::setCategorySpinnerData);
        configureNotificationTimeSpinner();
    }

    private void setCategorySpinnerData(List<Category> categoryList) {
        categoryList.add(0, new Category(0, ""));
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCategory.setAdapter(adapter);

        Long chosenCategory = appPreferences.getChosenCategory();
        if (chosenCategory != null) {
            int selectedItem = categoryList.indexOf(new Category(chosenCategory, ""));
            sCategory.setSelection(selectedItem);
        }
    }

    private void configureNotificationTimeSpinner() {
        ArrayList<NotificationTimeSpinnerItem> spinnerData = prepareNotificationTimeSpinnerData();
        ArrayAdapter<NotificationTimeSpinnerItem> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                spinnerData
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sNotificationTime.setAdapter(adapter);

        int selectedItem = spinnerData.indexOf(
                new NotificationTimeSpinnerItem(null, appPreferences.getNotificationBeforeCompletionMs())
        );
        sNotificationTime.setSelection(selectedItem);
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

    private void onFormSubmit() {
        appPreferences.setHideDoneTasks(cbHideDoneTasks.isChecked());
        handleChosenCategorySpinner();
        handleNotificationTimeSpinner();
        appPreferences.saveAppPreferences();
    }

    private void handleChosenCategorySpinner() {
        Category chosenCategory = (Category) sCategory.getSelectedItem();
        if (chosenCategory != null) {
            appPreferences.setChosenCategory(chosenCategory.getCategoryId());
        }
    }

    private void handleNotificationTimeSpinner() {
        NotificationTimeSpinnerItem item = (NotificationTimeSpinnerItem) sNotificationTime.getSelectedItem();
        if (item != null) {
            appPreferences.setNotificationBeforeCompletionMs(item.getValue());
        }
    }

    public void applyButtonOnClick(View view) {
        onFormSubmit();
        displayToast(this, "Settings applied successfully");
        finish();
    }
}