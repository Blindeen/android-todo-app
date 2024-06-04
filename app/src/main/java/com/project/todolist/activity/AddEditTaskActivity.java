package com.project.todolist.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.todolist.MainActivity;
import com.project.todolist.R;
import com.project.todolist.db.entity.Category;
import com.project.todolist.db.entity.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.project.todolist.Utils.*;
import static com.project.todolist.notification.NotificationUtils.*;

public class AddEditTaskActivity extends MainActivity {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "MMM dd, yyyy hh:mm a"
    );

    private Task task;
    boolean isEdit = false;

    private TextView headerLabel;
    private EditText titleInput, descriptionInput, dateTimeInput;
    private Spinner categorySpinner;
    private CheckBox notificationCheckbox;
    private Button deleteButton;

    private Disposable taskQuerySubscriber;
    private Disposable deleteTaskQuerySubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_task);
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

        initializeWidgets();
        Intent intent = getIntent();
        Task task = intent.getSerializableExtra("task", Task.class);
        configureForm(task);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (categoryListQuerySubscriber != null) {
            categoryListQuerySubscriber.dispose();
        }

        if (taskQuerySubscriber != null) {
            taskQuerySubscriber.dispose();
        }

        if (deleteTaskQuerySubscriber != null) {
            deleteTaskQuerySubscriber.dispose();
        }
    }

    private void initializeWidgets() {
        headerLabel = findViewById(R.id.text_add_edit_header);
        titleInput = findViewById(R.id.text_task_title);
        descriptionInput = findViewById(R.id.text_task_description);
        categorySpinner = findViewById(R.id.spinner_category);
        dateTimeInput = findViewById(R.id.text_completion_date);
        notificationCheckbox = findViewById(R.id.checkbox_notification);
        deleteButton = findViewById(R.id.button_delete);
    }

    private void configureForm(Task task) {
        if (task != null) {
            this.task = task;
            isEdit = true;

            titleInput.setText(task.getTitle());
            descriptionInput.setText(task.getDescription());
            dateTimeInput.setText(task.getDoneAt());
            notificationCheckbox.setChecked(task.isNotification());

            fetchCategories(categoryList -> {
                setCategorySpinnerData(categoryList);
                int position = categoryList.indexOf(new Category(task.getCategoryId(), ""));
                categorySpinner.setSelection(position);
            });
        } else {
            fetchCategories(this::setCategorySpinnerData);
        }

        headerLabel.setText(isEdit ? R.string.edit_task_header : R.string.new_task_header);
        deleteButton.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        configTaskCompletionPicker();
    }

    private void setCategorySpinnerData(List<Category> categoryList) {
        categoryList.add(0, new Category(0, ""));
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void configTaskCompletionPicker() {
        dateTimeInput.setOnClickListener(v -> showDateTimePicker());
    }

    private void showDateTimePicker() {
        Calendar calendar = prepareCalendar(dateTimeInput.getText().toString());
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                String dateTimeString = LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).format(DATE_TIME_FORMATTER);
                dateTimeInput.setText(dateTimeString);
                dateTimeInput.setError(null);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private Calendar prepareCalendar(String completionDateString) {
        Calendar calendar = Calendar.getInstance();
        LocalDateTime dateTime = parseDateTimePickerString(completionDateString, DATE_TIME_FORMATTER);
        if (dateTime != null) {
            calendar.set(Calendar.YEAR, dateTime.getYear());
            calendar.set(Calendar.MONTH, dateTime.getMonthValue() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, dateTime.getHour());
            calendar.set(Calendar.MINUTE, dateTime.getMinute());
        }

        return calendar;
    }

    private boolean validateForm() {
        boolean isValid = true;

        TextView[] inputWidgets = {titleInput, descriptionInput, dateTimeInput};
        for (TextView widget : inputWidgets) {
            EditText etWidget = (EditText) widget;
            if (etWidget.length() == 0) {
                etWidget.setError("Field cannot be empty");
                isValid = false;
            }
        }

        Category chosenCategory = (Category) categorySpinner.getSelectedItem();
        if (chosenCategory.getName().isEmpty()) {
            TextView errorText = (TextView) categorySpinner.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            errorText.setText(R.string.spinner_category_error);

            isValid = false;
        }

        String dateTimeString = dateTimeInput.getText().toString();
        if (!isDateTimeValid(dateTimeString, DATE_TIME_FORMATTER)) {
            dateTimeInput.setError("Invalid date time value");
        }

        return isValid;
    }

    private void createTask() {
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        Category category = (Category) categorySpinner.getSelectedItem();
        String dateTime = dateTimeInput.getText().toString();
        boolean notification = notificationCheckbox.isChecked();

        task = new Task(title, description, dateTime, notification, category.getCategoryId());

        Completable completable = database.taskDao().insertTask(
                task.getTitle(),
                task.getDescription(),
                task.getDoneAt(),
                task.getCategoryId(),
                task.isNotification()
        );
        taskQuerySubscriber = completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    displayToast(this, "New task has been added successfully");
                    finish();
                }, throwable -> displayToast(this, "Failed to add new task"));

        if (notification) {
            scheduleNotification(this, task);
        }
    }

    private void updateTask() {
        task.setTitle(titleInput.getText().toString());
        task.setDescription(descriptionInput.getText().toString());
        task.setDoneAt(dateTimeInput.getText().toString());
        task.setNotification(notificationCheckbox.isChecked());
        Category category = (Category) categorySpinner.getSelectedItem();
        task.setCategoryId(category.getCategoryId());

        Completable completable = database.taskDao().updateTask(task);
        taskQuerySubscriber = completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    displayToast(this, "Task has been updated successfully");
                    finish();
                }, throwable -> displayToast(this, "Failed to update task"));
    }

    private void saveTask() {
        if (!validateForm()) {
            return;
        }

        if (isEdit) {
            updateTask();
        } else {
            createTask();
        }
    }

    public void addButtonOnClick(View view) {
        saveTask();
    }

    private void deleteTask() {
        Completable completable = database.taskDao().deleteTask(task);
        deleteTaskQuerySubscriber = completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    displayToast(this, "Task has been deleted successfully");
                    finish();
                }, throwable -> displayToast(this, "Failed to delete task"));

        if (task.isNotification()) {
            cancelNotification(this, task);
        }
    }

    public void deleteButtonOnClick(View view) {
        deleteTask();
    }
}