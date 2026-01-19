package me.pinitnotification.application.notification;

import me.pinitnotification.application.push.PushSendResult;
import me.pinitnotification.application.push.PushService;
import me.pinitnotification.domain.notification.UpcomingScheduleNotification;
import me.pinitnotification.domain.notification.UpcomingScheduleNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchSchedulerTest {

    @Mock
    private UpcomingScheduleNotificationRepository notificationRepository;
    @Mock
    private NotificationDispatchQueryRepository dispatchQueryRepository;
    @Mock
    private PushTokenCleanupService pushTokenCleanupService;
    @Mock
    private PushService pushService;

    private NotificationDispatchScheduler scheduler;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2024-06-01T10:00:00Z"), ZoneOffset.UTC);
        scheduler = new NotificationDispatchScheduler(notificationRepository, dispatchQueryRepository, pushTokenCleanupService, pushService, clock);
    }

    @Test
    void dispatchDueNotifications_sendsAndDeletesNotificationsFromQuery() {
        UpcomingScheduleNotification notification = new UpcomingScheduleNotification(1L, 10L, "title", "2024-06-01T09:50Z", "key-1");

        when(dispatchQueryRepository.findAllDueNotificationsWithTokens(any()))
                .thenReturn(List.of(new NotificationDispatchItem(notification, List.of("token-1", "token-2"))));
        when(pushService.sendPushMessage(anyString(), eq(notification))).thenReturn(PushSendResult.successResult());

        scheduler.dispatchDueNotifications();

        verify(pushService).sendPushMessage("token-1", notification);
        verify(pushService).sendPushMessage("token-2", notification);
        verify(notificationRepository).deleteAllInBatch(List.of(notification));
        verify(pushTokenCleanupService, never()).deleteTokensInNewTransaction(any());
    }

    @Test
    void dispatchDueNotifications_deletesEvenWhenNoTokens() {
        UpcomingScheduleNotification past = new UpcomingScheduleNotification(2L, 20L, "title", "2024-06-01T09:00Z", "key-3");

        when(dispatchQueryRepository.findAllDueNotificationsWithTokens(any()))
                .thenReturn(List.of(new NotificationDispatchItem(past, List.of())));

        scheduler.dispatchDueNotifications();

        verify(pushService, never()).sendPushMessage(anyString(), any());
        verify(notificationRepository).deleteAllInBatch(List.of(past));
        verify(pushTokenCleanupService, never()).deleteTokensInNewTransaction(any());
    }

    @Test
    void dispatchDueNotifications_collectsInvalidTokensAndDeletesInBatch() {
        UpcomingScheduleNotification notification = new UpcomingScheduleNotification(3L, 30L, "title", "2024-06-01T09:30Z", "key-4");

        when(dispatchQueryRepository.findAllDueNotificationsWithTokens(any()))
                .thenReturn(List.of(new NotificationDispatchItem(notification, List.of("token-1", "token-2", "token-3"))));
        when(pushService.sendPushMessage("token-1", notification)).thenReturn(PushSendResult.invalidTokenResult());
        when(pushService.sendPushMessage("token-2", notification)).thenReturn(PushSendResult.failedResult());
        when(pushService.sendPushMessage("token-3", notification)).thenReturn(PushSendResult.successResult());

        scheduler.dispatchDueNotifications();

        verify(pushTokenCleanupService).deleteTokensInNewTransaction(Set.of("token-1"));
        verify(notificationRepository).deleteAllInBatch(List.of(notification));
    }
}
