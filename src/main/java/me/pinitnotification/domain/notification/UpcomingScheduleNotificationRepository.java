package me.pinitnotification.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UpcomingScheduleNotificationRepository extends JpaRepository<UpcomingScheduleNotification, Long> {
    Optional<UpcomingScheduleNotification> findByScheduleIdAndOwnerId(Long scheduleId, Long ownerId);
    boolean existsByScheduleIdAndOwnerId(Long scheduleId, Long ownerId);
    void deleteByScheduleIdAndOwnerId(Long scheduleId, Long ownerId);
}
