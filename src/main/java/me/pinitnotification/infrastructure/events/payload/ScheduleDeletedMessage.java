package me.pinitnotification.infrastructure.events.payload;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ScheduleDeletedMessage(
        @NotNull Long ownerId,
        @NotNull Long scheduleId,
        @NotNull OffsetDateTime occurredAt,
        @NotNull String idempotentKey
) {
}
