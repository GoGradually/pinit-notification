package me.pinitnotification.application.notification;

import java.time.Instant;
import java.util.List;

public interface NotificationDispatchQueryRepository {
    List<NotificationDispatchItem> findDueNotificationsWithTokens(Instant now);
}
