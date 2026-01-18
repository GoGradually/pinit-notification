package me.pinitnotification.domain.notification;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@Getter
public class UpcomingScheduleNotification implements Notification {
    private UUID id;
    private Long ownerId;
    private Long scheduleId;
    private String scheduleTitle;
    private String scheduleStartTime;
    private String idempotentKey;

    protected UpcomingScheduleNotification() {}

    public UpcomingScheduleNotification(UUID id, Long ownerId, Long scheduleId, String scheduleTitle, String scheduleStartTime, String idempotentKey) {
        this.id = id;
        this.ownerId = ownerId;
        this.scheduleId = scheduleId;
        this.scheduleTitle = scheduleTitle;
        this.scheduleStartTime = scheduleStartTime;
        this.idempotentKey = idempotentKey;
    }

    public UpcomingScheduleNotification(Long ownerId, Long scheduleId, String scheduleTitle, String scheduleStartTime, String idempotentKey) {
        this.ownerId = ownerId;
        this.scheduleId = scheduleId;
        this.scheduleTitle = scheduleTitle;
        this.scheduleStartTime = scheduleStartTime;
        this.idempotentKey = idempotentKey;
    }

    @Override
    public Map<String, String> getData() {
        return Map.of("scheduleId", String.valueOf(scheduleId),
                "scheduleTitle", scheduleTitle,
                "scheduleStartTime", scheduleStartTime,
                "idempotentKey", idempotentKey);
    }

    public void updateScheduleStartTime(String scheduleStartTime, String idempotentKey) {
        this.scheduleStartTime = scheduleStartTime;
        this.idempotentKey = idempotentKey;
    }

    public boolean isDue(OffsetDateTime now) {
        if (scheduleStartTime == null) {
            return false;
        }
        try {
            OffsetDateTime startTime = OffsetDateTime.parse(scheduleStartTime);
            return !startTime.isAfter(now);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
