package com.project.todolist.notification;

import android.app.job.JobParameters;
import android.app.job.JobService;

import static com.project.todolist.notification.NotificationUtils.showNotification;

public class NotificationJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        String taskTitle = params.getExtras().getString("title");
        new Thread(() -> {
            showNotification(this, "Task Reminder", taskTitle);
            jobFinished(params, false);
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
