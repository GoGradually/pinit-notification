package me.pinitnotification.domain.notification;


import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Getter
@Entity
@Table(
        name = "upcoming_schedule_notification",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_schedule_owner", columnNames = {"schedule_id", "owner_id"}),
                @UniqueConstraint(name = "uk_idempotent_key", columnNames = {"idempotent_key"})
        }
)
public class UpcomingScheduleNotification implements Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;
    @Column(name = "schedule_title", nullable = false)
    private String scheduleTitle;
    @Column(name = "schedule_start_time", nullable = false)
    private String scheduleStartTime;
    @Column(name = "idempotent_key", nullable = false)
    private String idempotentKey;

    protected UpcomingScheduleNotification() {}

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
