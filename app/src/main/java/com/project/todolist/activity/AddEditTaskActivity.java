package com.project.todolist.activity;

import static com.project.todolist.Utils.displayToast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.todolist.MainActivity;
import com.project.todolist.R;
import com.project.todolist.db.AppDatabase;
import com.project.todolist.db.entity.Category;
import com.project.todolist.db.entity.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.List;

public class AddEditTaskActivity extends MainActivity {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

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

        database = AppDatabase.getDatabase(this);
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
        fetchCategories(this::setCategorySpinnerData);
        configTaskCompletionPicker();
    }

    private void setCategorySpinnerData(List<Category> categoryList) {
        Spinner categorySpinner = findViewById(R.id.spinner_category);

        categoryList.add(0, new Category(0, ""));
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        if (chosenCategory != null) {
            int selectedItem = categoryList.indexOf(new Category(chosenCategory, ""));
            categorySpinner.setSelection(selectedItem);
        }
    }

    private void configTaskCompletionPicker() {
        EditText textCompletionDate = findViewById(R.id.text_completion_date);
        textCompletionDate.setOnClickListener(v -> showDateTimePicker(textCompletionDate));
    }

    private void showDateTimePicker(EditText textCompletionDate) {
        Calendar calendar = prepareCalendar(textCompletionDate.getText().toString());
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                String dateTimeString = LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).format(DATE_TIME_FORMATTER);
                textCompletionDate.setText(dateTimeString);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private Calendar prepareCalendar(String completionDateString) {
        Calendar calendar = Calendar.getInstance();
        LocalDateTime dateTime = parseDateTimePickerString(completionDateString);
        if (dateTime != null) {
            calendar.set(Calendar.YEAR, dateTime.getYear());
            calendar.set(Calendar.MONTH, dateTime.getMonthValue() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, dateTime.getHour());
            calendar.set(Calendar.MINUTE, dateTime.getMinute());
        }

        return calendar;
    }

    private LocalDateTime parseDateTimePickerString(String completionDateString) {
        try {
            return LocalDateTime.parse(completionDateString, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Task createTask() {
        EditText titleInput = findViewById(R.id.text_task_title);
        String title = titleInput.getText().toString();

        EditText descriptionInput = findViewById(R.id.text_task_description);
        String description = descriptionInput.getText().toString();

        Spinner categorySpinner = findViewById(R.id.spinner_category);
        Category category = (Category) categorySpinner.getSelectedItem();

        EditText dateTimeInput = findViewById(R.id.text_completion_date);
        String dateTime = dateTimeInput.getText().toString();

        CheckBox notificationCheckbox = findViewById(R.id.checkbox_notification);
        boolean isChecked = notificationCheckbox.isChecked();

        return new Task(title, description, dateTime, isChecked, category.getCategoryId());
    }

    private void saveTask() {
        Task newTask = createTask();

    }

    public void addButtonOnClick(View view) {
        displayToast(this, "New task has been added successfully");
        finish();
    }
}