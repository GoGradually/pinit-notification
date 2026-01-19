package me.pinitnotification.application.notification;

import me.pinitnotification.domain.notification.UpcomingScheduleNotification;

import java.util.List;

public record NotificationDispatchItem(
        UpcomingScheduleNotification notification,
        List<String> tokens
) {
}
