package com.project.todolist;

import android.content.Context;
import android.widget.Toast;

public class Utils {
    public static void displayToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
