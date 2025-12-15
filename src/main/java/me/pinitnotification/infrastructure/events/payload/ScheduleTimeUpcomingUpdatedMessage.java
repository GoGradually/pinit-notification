package me.pinitnotification.infrastructure.events.payload;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ScheduleTimeUpcomingUpdatedMessage(
        @NotNull Long ownerId,
        @NotNull Long scheduleId,
        @NotNull OffsetDateTime newUpcomingTime,
        @NotNull OffsetDateTime occurredAt,
        @NotNull String idempotentKey
) {
}
