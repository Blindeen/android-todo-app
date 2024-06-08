package com.project.todolist.notification;

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

import com.project.todolist.MainActivity;
import com.project.todolist.R;
import com.project.todolist.database.entity.Task;

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

    private static JobInfo createNotificationJobInfo(Context context, Task task, long latency) {
        int jobId = (int) task.getCategoryId();
        String taskTitle = task.getTitle();
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("title", taskTitle);
        return new JobInfo.Builder(jobId, new ComponentName(context, NotificationJobService.class))
                .setMinimumLatency(latency)
                .setExtras(bundle)
                .build();

    }

    public static void scheduleNotification(Context context, Task task, long latency) {
        JobInfo jobInfo = createNotificationJobInfo(context, task, latency);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(jobInfo);
    }

    public static void cancelNotification(Context context, Task task) {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancel((int) task.getCategoryId());
    }
}
