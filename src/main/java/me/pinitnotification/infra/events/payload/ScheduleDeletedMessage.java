package me.pinitnotification.infra.events.payload;

import java.time.OffsetDateTime;

public record ScheduleDeletedMessage(
        Long ownerId,
        Long scheduleId,
        OffsetDateTime occurredAt,
        String idempotentKey
) {
}
