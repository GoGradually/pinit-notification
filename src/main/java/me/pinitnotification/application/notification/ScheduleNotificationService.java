package me.pinitnotification.application.notification;

import me.pinitnotification.application.notification.command.ScheduleDeletedCommand;
import me.pinitnotification.application.notification.command.ScheduleStateChangedCommand;
import me.pinitnotification.application.notification.command.UpcomingUpdatedCommand;
import me.pinitnotification.application.notification.query.ScheduleBasics;
import me.pinitnotification.application.notification.query.ScheduleQueryPort;
import me.pinitnotification.domain.notification.UpcomingScheduleNotification;
import me.pinitnotification.domain.notification.UpcomingScheduleNotificationRepository;
import me.pinitnotification.domain.shared.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleNotificationService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleNotificationService.class);

    private final UpcomingScheduleNotificationRepository notificationRepository;
    private final ScheduleQueryPort scheduleQueryPort;
    private final IdGenerator idGenerator;

    public ScheduleNotificationService(UpcomingScheduleNotificationRepository notificationRepository,
                                       ScheduleQueryPort scheduleQueryPort,
                                       IdGenerator idGenerator) {
        this.notificationRepository = notificationRepository;
        this.scheduleQueryPort = scheduleQueryPort;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public void handleUpcomingUpdated(UpcomingUpdatedCommand command) {
        notificationRepository.findByScheduleIdAndOwnerId(command.scheduleId(), command.ownerId())
                .ifPresentOrElse(
                        existing -> existing.updateScheduleStartTime(resolveScheduleStartTime(command, existing), command.idempotentKey()),
                        () -> notificationRepository.save(buildNotification(command.ownerId(), command.scheduleId(), command.idempotentKey(), toStringValue(command.newUpcomingTime())))
                );
    }

    @Transactional
    public void handleScheduleDeleted(ScheduleDeletedCommand command) {
        if (notificationRepository.existsByScheduleIdAndOwnerId(command.scheduleId(), command.ownerId())) {
            notificationRepository.deleteByScheduleIdAndOwnerId(command.scheduleId(), command.ownerId());
        } else {
            log.debug("Skip deleting notification. scheduleId={}, ownerId={} not found", command.scheduleId(), command.ownerId());
        }
    }

    @Transactional
    public void handleScheduleStarted(ScheduleStateChangedCommand command) {
        if (notificationRepository.existsByScheduleIdAndOwnerId(command.scheduleId(), command.ownerId())) {
            notificationRepository.deleteByScheduleIdAndOwnerId(command.scheduleId(), command.ownerId());
        } else {
            log.debug("Skip deleting notification on started. scheduleId={}, ownerId={} not found", command.scheduleId(), command.ownerId());
        }
    }

    @Transactional
    public void handleScheduleCanceled(ScheduleStateChangedCommand command) {
        if (notificationRepository.existsByScheduleIdAndOwnerId(command.scheduleId(), command.ownerId())) {
            log.debug("Notification already exists for canceled state. scheduleId={}, ownerId={}", command.scheduleId(), command.ownerId());
            return;
        }
        notificationRepository.save(buildNotification(command.ownerId(), command.scheduleId(), command.idempotentKey(), null));
    }

    @Transactional
    public void handleScheduleCompleted(ScheduleStateChangedCommand command) {
        if (notificationRepository.existsByScheduleIdAndOwnerId(command.scheduleId(), command.ownerId())) {
            notificationRepository.deleteByScheduleIdAndOwnerId(command.scheduleId(), command.ownerId());
        } else {
            log.debug("Skip deleting notification on completed. scheduleId={}, ownerId={} not found", command.scheduleId(), command.ownerId());
        }
    }

    private UpcomingScheduleNotification buildNotification(Long ownerId, Long scheduleId, String idempotentKey, String scheduleStartTimeOverride) {
        ScheduleBasics basics = scheduleQueryPort.getScheduleBasics(scheduleId, ownerId);
        String scheduleStartTime = scheduleStartTimeOverride != null ? scheduleStartTimeOverride : basics.designatedStartTime();

        return new UpcomingScheduleNotification(
                idGenerator.generate(),
                basics.ownerId(),
                basics.scheduleId(),
                basics.scheduleTitle(),
                scheduleStartTime,
                idempotentKey
        );
    }

    private String toStringValue(java.time.OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }

    private String resolveScheduleStartTime(UpcomingUpdatedCommand command, UpcomingScheduleNotification existing) {
        String newStartTime = toStringValue(command.newUpcomingTime());
        return newStartTime != null ? newStartTime : existing.getScheduleStartTime();
    }
}
