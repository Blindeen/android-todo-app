package com.project.todolist;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

public class Utils {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "MMM dd, yyyy hh:mm a"
    );

    public static void displayToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static LocalDateTime parseDateTimeString(String dateTime, DateTimeFormatter formatter) {
        try {
            return LocalDateTime.parse(dateTime, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isDateTimeValid(String dateTime) {
        try {
            LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static long calculateLatency(String dateTime, long periodBefore) {
        LocalDateTime dateTimeValue = parseDateTimeString(dateTime, DATE_TIME_FORMATTER);
        ZonedDateTime zdt = ZonedDateTime.of(dateTimeValue, ZoneId.systemDefault());
        long millis = zdt.toInstant().toEpochMilli();
        return millis - System.currentTimeMillis() - periodBefore;
    }

    public static Calendar prepareCalendar(String dateTimeString) {
        Calendar calendar = Calendar.getInstance();
        LocalDateTime dateTime = parseDateTimeString(dateTimeString, DATE_TIME_FORMATTER);
        if (dateTime != null) {
            calendar.set(Calendar.YEAR, dateTime.getYear());
            calendar.set(Calendar.MONTH, dateTime.getMonthValue() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, dateTime.getHour());
            calendar.set(Calendar.MINUTE, dateTime.getMinute());
        }

        return calendar;
    }

    public static void copyFile(Context context, Uri sourceUri) {
        String sourceFilename = sourceUri.getLastPathSegment();
        String fileExtension = getFileExtension(context, sourceUri);
        String pathname = context.getFilesDir() + "/" + sourceFilename + fileExtension;
        File destinationFile = new File(pathname);

        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             FileOutputStream out = new FileOutputStream(destinationFile)
        ) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            displayToast(context, "Failed to copy file");
        }
    }

    private static String getFileExtension(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

        return "." + extension;
    }
}
