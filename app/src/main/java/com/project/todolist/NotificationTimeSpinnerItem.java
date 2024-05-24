package com.project.todolist;

import androidx.annotation.NonNull;

public class NotificationTimeSpinnerItem {
    private final String displayString;
    private final int value;

    public NotificationTimeSpinnerItem(String displayString, int value) {
        this.displayString = displayString;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return displayString;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NotificationTimeSpinnerItem) {
            NotificationTimeSpinnerItem item = (NotificationTimeSpinnerItem) o;
            return value == item.getValue();
        }

        return false;
    }
}
