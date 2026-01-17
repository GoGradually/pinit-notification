package me.pinitnotification.application.notification;

import me.pinitnotification.application.notification.command.ScheduleDeletedCommand;
import me.pinitnotification.application.notification.command.ScheduleStateChangedCommand;
import me.pinitnotification.application.notification.command.UpcomingUpdatedCommand;
import me.pinitnotification.application.notification.query.ScheduleBasics;
import me.pinitnotification.application.notification.query.ScheduleQueryPort;
import me.pinitnotification.domain.notification.UpcomingScheduleNotification;
import me.pinitnotification.domain.notification.UpcomingScheduleNotificationRepository;
import me.pinitnotification.domain.shared.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleNotificationServiceTest {

    @Mock
    private UpcomingScheduleNotificationRepository notificationRepository;
    @Mock
    private ScheduleQueryPort scheduleQueryPort;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private ScheduleNotificationService scheduleNotificationService;

    @Captor
    private ArgumentCaptor<UpcomingScheduleNotification> notificationCaptor;

    private final Long ownerId = 1L;
    private final Long scheduleId = 10L;

    @BeforeEach
    void resetMocks() {
        reset(notificationRepository, scheduleQueryPort, idGenerator);
    }

    @Test
    void handleUpcomingUpdated_updatesExistingNotification() {
        UpcomingScheduleNotification existing = new UpcomingScheduleNotification(ownerId, scheduleId, "title", "2024-01-01T00:00Z", "old-key");
        when(notificationRepository.findByScheduleIdAndOwnerId(scheduleId, ownerId)).thenReturn(Optional.of(existing));

        UpcomingUpdatedCommand command = new UpcomingUpdatedCommand(
                ownerId, scheduleId, OffsetDateTime.parse("2024-02-01T12:00:00Z"), "new-key"
        );

        scheduleNotificationService.handleUpcomingUpdated(command);

        assertThat(existing.getScheduleStartTime()).isEqualTo("2024-02-01T12:00Z");
        assertThat(existing.getIdempotentKey()).isEqualTo("new-key");
        verify(notificationRepository, never()).save(any());
        verifyNoInteractions(scheduleQueryPort);
    }

    @Test
    void handleUpcomingUpdated_createsWhenMissing() {
        when(notificationRepository.findByScheduleIdAndOwnerId(scheduleId, ownerId)).thenReturn(Optional.empty());
        when(scheduleQueryPort.getScheduleBasics(scheduleId, ownerId))
                .thenReturn(new ScheduleBasics(scheduleId, ownerId, "title", "2024-03-01T00:00:00Z"));
        when(idGenerator.generate()).thenReturn(java.util.UUID.randomUUID());

        UpcomingUpdatedCommand command = new UpcomingUpdatedCommand(
                ownerId, scheduleId, OffsetDateTime.parse("2024-04-01T09:30:00Z"), "key-123"
        );

        scheduleNotificationService.handleUpcomingUpdated(command);

        verify(notificationRepository).save(notificationCaptor.capture());
        UpcomingScheduleNotification saved = notificationCaptor.getValue();
        assertThat(saved.getOwnerId()).isEqualTo(ownerId);
        assertThat(saved.getScheduleId()).isEqualTo(scheduleId);
        assertThat(saved.getScheduleTitle()).isEqualTo("title");
        assertThat(saved.getScheduleStartTime()).isEqualTo("2024-04-01T09:30Z");
        assertThat(saved.getIdempotentKey()).isEqualTo("key-123");
    }

    @Test
    void handleScheduleDeleted_deletesWhenExists() {
        when(notificationRepository.existsByScheduleIdAndOwnerId(scheduleId, ownerId)).thenReturn(true);
        ScheduleDeletedCommand command = new ScheduleDeletedCommand(ownerId, scheduleId, "del-key");

        scheduleNotificationService.handleScheduleDeleted(command);

        verify(notificationRepository).deleteByScheduleIdAndOwnerId(scheduleId, ownerId);
    }

    @Test
    void handleScheduleCanceled_createsWhenMissing() {
        when(notificationRepository.existsByScheduleIdAndOwnerId(scheduleId, ownerId)).thenReturn(false);
        when(scheduleQueryPort.getScheduleBasics(scheduleId, ownerId))
                .thenReturn(new ScheduleBasics(scheduleId, ownerId, "canceled title", "2024-05-10T10:00:00Z"));
        when(idGenerator.generate()).thenReturn(java.util.UUID.randomUUID());
        ScheduleStateChangedCommand command = new ScheduleStateChangedCommand(ownerId, scheduleId, "BEFORE", "cancel-key");

        scheduleNotificationService.handleScheduleCanceled(command);

        verify(notificationRepository).save(notificationCaptor.capture());
        UpcomingScheduleNotification saved = notificationCaptor.getValue();
        assertThat(saved.getScheduleTitle()).isEqualTo("canceled title");
        assertThat(saved.getScheduleStartTime()).isEqualTo("2024-05-10T10:00:00Z");
        assertThat(saved.getIdempotentKey()).isEqualTo("cancel-key");
    }

    @Test
    void handleScheduleCanceled_skipsWhenExists() {
        when(notificationRepository.existsByScheduleIdAndOwnerId(scheduleId, ownerId)).thenReturn(true);
        ScheduleStateChangedCommand command = new ScheduleStateChangedCommand(ownerId, scheduleId, "BEFORE", "cancel-key");

        scheduleNotificationService.handleScheduleCanceled(command);

        verify(notificationRepository, never()).save(any());
        verifyNoInteractions(scheduleQueryPort);
    }

    @Test
    void handleScheduleStarted_deletesWhenExists() {
        when(notificationRepository.existsByScheduleIdAndOwnerId(scheduleId, ownerId)).thenReturn(true);
        ScheduleStateChangedCommand command = new ScheduleStateChangedCommand(ownerId, scheduleId, "BEFORE", "start-key");

        scheduleNotificationService.handleScheduleStarted(command);

        verify(notificationRepository).deleteByScheduleIdAndOwnerId(scheduleId, ownerId);
    }

    @Test
    void handleScheduleCompleted_deletesWhenExists() {
        when(notificationRepository.existsByScheduleIdAndOwnerId(scheduleId, ownerId)).thenReturn(true);
        ScheduleStateChangedCommand command = new ScheduleStateChangedCommand(ownerId, scheduleId, "BEFORE", "complete-key");

        scheduleNotificationService.handleScheduleCompleted(command);

        verify(notificationRepository).deleteByScheduleIdAndOwnerId(scheduleId, ownerId);
    }
}
