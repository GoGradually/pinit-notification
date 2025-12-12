package me.pinitnotification.infra.events.payload;

import java.time.OffsetDateTime;

public record ScheduleStateChangedMessage(
        Long ownerId,
        Long scheduleId,
        String beforeState,
        OffsetDateTime occurredAt,
        String idempotentKey
) {
}
