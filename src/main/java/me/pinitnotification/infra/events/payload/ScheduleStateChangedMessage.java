package me.pinitnotification.infra.events.payload;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ScheduleStateChangedMessage(
        @NotNull Long ownerId,
        @NotNull Long scheduleId,
        @NotNull String beforeState,
        @NotNull OffsetDateTime occurredAt,
        @NotNull String idempotentKey
) {
}
