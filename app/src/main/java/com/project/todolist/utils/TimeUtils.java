package com.project.todolist.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

public class TimeUtils {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "MMM dd, yyyy hh:mm a"
    );

    public static Calendar prepareCalendar(String dateTimeString) {
        Calendar calendar = Calendar.getInstance();
        LocalDateTime dateTime = parseDateTimeString(dateTimeString);
        if (dateTime != null) {
            calendar.set(Calendar.YEAR, dateTime.getYear());
            calendar.set(Calendar.MONTH, dateTime.getMonthValue() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, dateTime.getHour());
            calendar.set(Calendar.MINUTE, dateTime.getMinute());
        }

        return calendar;
    }

    private static LocalDateTime parseDateTimeString(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
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
        LocalDateTime dateTimeValue = parseDateTimeString(dateTime);
        ZonedDateTime zdt = ZonedDateTime.of(dateTimeValue, ZoneId.systemDefault());
        long millis = zdt.toInstant().toEpochMilli();
        return millis - System.currentTimeMillis() - periodBefore;
    }
}
