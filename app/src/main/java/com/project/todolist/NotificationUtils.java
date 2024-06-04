package com.project.todolist;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

import androidx.core.app.NotificationCompat;

import com.project.todolist.db.entity.Task;
import com.project.todolist.jobservice.NotificationService;

public class NotificationUtils {
    private static int notificationId = 0;

    public static void createNotificationChannel(Context context) {
        String channelId = context.getString(R.string.notification_channel_id);
        CharSequence name = context.getString(R.string.notification_channel_name);
        String description = context.getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private static Notification createNotification(Context context, String title, String message) {
        String channelId = context.getString(R.string.notification_channel_id);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }

    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        Notification notification = createNotification(context, title, message);
        notificationManager.notify(notificationId++, notification);
    }

    private static JobInfo createNotificationJobInfo(Context context, Task task) {
        int jobId = (int) task.getCategoryId();
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("title", task.getTitle());
        return new JobInfo.Builder(jobId,
                new ComponentName(context, NotificationService.class))
                .setMinimumLatency(1000)
                .setExtras(bundle)
                .build();

    }

    public static void scheduleNotification(Context context, Task task) {
        JobInfo jobInfo = createNotificationJobInfo(context, task);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }
}
