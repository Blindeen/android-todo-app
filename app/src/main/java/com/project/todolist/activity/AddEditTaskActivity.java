package com.project.todolist.activity;


import static com.project.todolist.notification.NotificationUtils.cancelNotification;
import static com.project.todolist.notification.NotificationUtils.scheduleNotification;
import static com.project.todolist.utils.FileUtils.copyFile;
import static com.project.todolist.utils.FileUtils.getFilenameWithExtension;
import static com.project.todolist.utils.TimeUtils.DATE_TIME_FORMATTER;
import static com.project.todolist.utils.TimeUtils.calculateLatency;
import static com.project.todolist.utils.TimeUtils.isDateTimeValid;
import static com.project.todolist.utils.TimeUtils.prepareCalendar;
import static com.project.todolist.utils.Utils.displayToast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.todolist.MainActivity;
import com.project.todolist.R;
import com.project.todolist.adapter.AttachmentListAdapter;
import com.project.todolist.database.entity.Attachment;
import com.project.todolist.database.entity.Category;
import com.project.todolist.database.entity.Task;
import com.project.todolist.database.entity.TaskWithAttachments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public class AddEditTaskActivity extends MainActivity {
    private Task task;
    boolean isEdit = false;

    private TextView headerLabel;
    private EditText titleInput, descriptionInput, dateTimeInput;
    private Spinner categorySpinner;
    private CheckBox notificationCheckbox;
    private Button deleteButton;

    private RecyclerView attachmentRecyclerView;
    private AttachmentListAdapter attachmentListAdapter;

    private Disposable taskQuerySubscriber;
    private Disposable deleteTaskQuerySubscriber;
    private Disposable attachmentListQuerySubscriber;
    private Disposable addAttachmentQuerySubscriber;

    private ActivityResultLauncher<Intent> filePickerLauncher;

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
        TaskWithAttachments taskWithAttachments = intent.getSerializableExtra("task", TaskWithAttachments.class);
        configureForm(taskWithAttachments);
        configFilePicker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Disposable[] disposables = {
                categoryListQuerySubscriber,
                taskQuerySubscriber,
                deleteTaskQuerySubscriber,
                attachmentListQuerySubscriber,
                addAttachmentQuerySubscriber
        };
        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
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
        configAttachmentRecycleViewer();
    }

    private void configAttachmentRecycleViewer() {
        attachmentRecyclerView = findViewById(R.id.recycler_attachment_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        attachmentRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void configureForm(TaskWithAttachments taskWithAttachments) {
        List<Attachment> attachments;
        if (taskWithAttachments != null) {
            Task task = taskWithAttachments.getTask();
            attachments = taskWithAttachments.getAttachments();
            this.task = task;
            isEdit = true;

            titleInput.setText(task.getTitle());
            descriptionInput.setText(task.getDescription());
            dateTimeInput.setText(task.getDoneAt());
            notificationCheckbox.setChecked(task.isNotification());

            databaseManager.fetchCategories(categoryList -> {
                setCategorySpinnerData(categoryList);
                int position = categoryList.indexOf(new Category(task.getCategoryId(), ""));
                categorySpinner.setSelection(position);
            });
        } else {
            attachments = new ArrayList<>();
            databaseManager.fetchCategories(this::setCategorySpinnerData);
        }

        setAttachmentRecyclerViewData(attachments);

        headerLabel.setText(isEdit ? R.string.edit_task_header : R.string.new_task_header);
        deleteButton.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        dateTimeInput.setOnClickListener(v -> showDateTimePicker());
    }

    private void setCategorySpinnerData(List<Category> categoryList) {
        categoryList.add(0, new Category(0, ""));
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setAttachmentRecyclerViewData(List<Attachment> attachments) {
        attachmentListAdapter = new AttachmentListAdapter(this, attachments);
        attachmentRecyclerView.setAdapter(attachmentListAdapter);
    }

    private void configFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null || data.getData() == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        Attachment attachment = createAttachment(uri, task);
                        copyFile(this, uri);
                        if (isEdit) {
                            addAttachmentQuerySubscriber = databaseManager.addAttachment(
                                    attachment,
                                    () -> attachmentListAdapter.addAttachment(attachment)
                            );
                        } else {
                            attachmentListAdapter.addAttachment(attachment);
                        }
                    }
                });
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

    private boolean validateForm() {
        boolean isValid = true;

        TextView[] inputWidgets = {titleInput, dateTimeInput};
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
        if (!isDateTimeValid(dateTimeString)) {
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
        taskQuerySubscriber = databaseManager.addTask(task, taskId -> {
            saveAttachments(taskId);
            displayToast(this, "Task has been added successfully");
            finish();
        });
    }

    private void updateTask() {
        task.setTitle(titleInput.getText().toString());
        task.setDescription(descriptionInput.getText().toString());
        task.setDoneAt(dateTimeInput.getText().toString());
        task.setNotification(notificationCheckbox.isChecked());
        Category category = (Category) categorySpinner.getSelectedItem();
        task.setCategoryId(category.getCategoryId());

        taskQuerySubscriber = databaseManager.updateTask(task, () -> {
            displayToast(this, "Task has been updated successfully");
            finish();
        });
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

        cancelNotification(this, task);
        if (notificationCheckbox.isChecked()) {
            handleNotification(dateTimeInput.getText().toString());
        }
    }

    private void saveAttachments(Long taskId) {
        List<Attachment> attachments = attachmentListAdapter.getData();
        for (Attachment attachment : attachments) {
            attachment.setTaskId(taskId);
        }

        addAttachmentQuerySubscriber = databaseManager.addAttachments(attachments);
    }

    private void handleNotification(String dateTime) {
        long latency = calculateLatency(dateTime, notificationBeforeCompletionMs);
        scheduleNotification(this, task, latency);
    }

    public void addButtonOnClick(View view) {
        saveTask();
    }

    public void deleteButtonOnClick(View view) {
        deleteTaskQuerySubscriber = databaseManager.deleteTask(task, () -> {
            displayToast(this, "Task has been deleted successfully");
            finish();
        });
        if (task.isNotification()) {
            cancelNotification(this, task);
        }
    }

    private void showFilePicker() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        filePickerLauncher.launch(chooseFile);
    }

    public void attachFileButtonOnClick(View view) {
        showFilePicker();
    }

    private Attachment createAttachment(Uri uri, Task task) {
        String filenameWithExtension = getFilenameWithExtension(this, uri);
        String pathname = getFilesDir() + "/" + filenameWithExtension;
        return new Attachment(filenameWithExtension, pathname, task != null ? task.getTaskId() : 0);
    }
}