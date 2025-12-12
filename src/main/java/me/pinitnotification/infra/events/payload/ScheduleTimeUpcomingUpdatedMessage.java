package me.pinitnotification.infra.events.payload;

import java.time.OffsetDateTime;

public record ScheduleTimeUpcomingUpdatedMessage(
        Long ownerId,
        Long scheduleId,
        OffsetDateTime newUpcomingTime,
        OffsetDateTime occurredAt,
        String idempotentKey
) {
}
