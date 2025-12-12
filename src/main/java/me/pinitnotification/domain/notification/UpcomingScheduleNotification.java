package me.pinitnotification.domain.notification;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.Map;

@Getter
@Entity
public class UpcomingScheduleNotification implements Notification {
    @Id
    @GeneratedValue
    private Long id;
    private Long scheduleId;
    private String scheduleTitle;
    private String scheduleStartTime;
    private String idempotentKey;

    protected UpcomingScheduleNotification() {}

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
