package me.pinitnotification.application.notification.query;

public interface ScheduleQueryPort {
    ScheduleBasics getScheduleBasics(Long scheduleId, Long ownerId);
}
