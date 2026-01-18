package me.pinitnotification.infrastructure.persistence.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.pinitnotification.infrastructure.persistence.UuidV7Generator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name = "upcoming_schedule_notification",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_schedule_owner", columnNames = {"schedule_id", "owner_id"}),
                @UniqueConstraint(name = "uk_idempotent_key", columnNames = {"idempotent_key"})
        }
)
@Getter
@Setter
public class UpcomingScheduleNotificationEntity {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "public_id", length = 36)
    private UUID publicId;

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

    protected UpcomingScheduleNotificationEntity() {
    }

    @PrePersist
    protected void assignPublicId() {
        if (publicId == null) {
            publicId = UuidV7Generator.generate();
        }
    }
}
