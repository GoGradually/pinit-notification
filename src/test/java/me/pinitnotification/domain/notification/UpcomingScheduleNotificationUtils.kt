package me.pinitnotification.domain.notification

import org.junit.platform.commons.util.ReflectionUtils

fun getSampleUpcomingScheduleNotification(
    id: Long = 1L,
    scheduleId: Long = 1L,
    scheduleTitle: String = "sample",
    scheduleStartTime: String = "2024-06-01T10:00:00Z",
    idempotencyKey: String = "",
): UpcomingScheduleNotification {
    val sample = UpcomingScheduleNotification(
        id,
        scheduleId,
        scheduleTitle,
        scheduleStartTime,
        idempotencyKey
    )
    ReflectionUtils.findFields(
        UpcomingScheduleNotification::class.java,
        { field -> field.name == "id" },
        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
    ).forEach { field ->
        field.isAccessible = true
        field.set(sample, 1L)
    }
    return sample
}