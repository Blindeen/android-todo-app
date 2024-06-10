package com.project.todolist;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final boolean HIDE_DONE_TASKS_DEFAULT = false;
    private static final Long CHOSEN_CATEGORY_ID_DEFAULT = null;
    private static final Integer NOTIFICATION_BEFORE_COMPLETION_MIN_DEFAULT = 0;

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private boolean hideDoneTasks;
    private Long chosenCategory;
    private Long notificationBeforeCompletionMs;

    public AppPreferences(Context context) {
        this.context = context;
        String sharedPreferencesFilename = context.getString(R.string.shared_pref_filename);
        sharedPreferences = context.getSharedPreferences(sharedPreferencesFilename, Context.MODE_PRIVATE);
        loadAppPreferences();
    }

    public void loadAppPreferences() {
        hideDoneTasks = sharedPreferences.getBoolean(
                context.getString(R.string.hide_done_tasks_key), HIDE_DONE_TASKS_DEFAULT
        );

        chosenCategory = sharedPreferences.getLong(
                context.getString(R.string.chosen_category_key), 0
        );
        if (chosenCategory == 0) {
            chosenCategory = CHOSEN_CATEGORY_ID_DEFAULT;
        }

        notificationBeforeCompletionMs = sharedPreferences.getLong(
                context.getString(R.string.notification_before_min_key),
                NOTIFICATION_BEFORE_COMPLETION_MIN_DEFAULT
        );
    }

    public boolean isHideDoneTasks() {
        return hideDoneTasks;
    }

    public void setHideDoneTasks(boolean hideDoneTasks) {
        this.hideDoneTasks = hideDoneTasks;
    }

    public Long getChosenCategory() {
        return chosenCategory;
    }

    public void setChosenCategory(Long chosenCategory) {
        this.chosenCategory = chosenCategory;
    }

    public Long getNotificationBeforeCompletionMs() {
        return notificationBeforeCompletionMs;
    }

    public void setNotificationBeforeCompletionMs(Long notificationBeforeCompletionMs) {
        this.notificationBeforeCompletionMs = notificationBeforeCompletionMs;
    }

    public void saveAppPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.hide_done_tasks_key), hideDoneTasks);
        editor.putLong(context.getString(R.string.notification_before_min_key), notificationBeforeCompletionMs);
        editor.putLong(context.getString(R.string.chosen_category_key), chosenCategory);
        editor.apply();
    }
}
