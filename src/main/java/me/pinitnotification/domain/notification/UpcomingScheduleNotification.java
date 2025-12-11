package me.pinitnotification.domain.notification;


import lombok.Getter;

import java.util.Map;

@Getter
public class UpcomingScheduleNotification implements Notification {
    private final Long scheduleId;
    private final String scheduleTitle;
    private final String scheduleStartTime;
    private final String idempotentKey;

    public UpcomingScheduleNotification(Long scheduleId, String scheduleTitle, String scheduleStartTime, String idempotentKey) {
        this.scheduleId = scheduleId;
        this.scheduleTitle = scheduleTitle;
        this.scheduleStartTime = scheduleStartTime;
        this.idempotentKey = idempotentKey;
    }

    @Override
    public Map<String, String> getData() {
        return Map.of("scheduleId", String.valueOf(scheduleId),
                "scheduleTitle", scheduleTitle,
                "idempotentKey", idempotentKey);
    }
}
