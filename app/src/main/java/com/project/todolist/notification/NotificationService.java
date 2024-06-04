package com.project.todolist.notification;

import android.app.job.JobParameters;
import android.app.job.JobService;

import static com.project.todolist.NotificationUtils.showNotification;

public class NotificationService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        String taskTitle = params.getExtras().getString("title");
        showNotification(this, "Task Reminder", taskTitle);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
