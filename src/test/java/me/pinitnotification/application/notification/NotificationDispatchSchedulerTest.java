package me.pinitnotification.application.notification;

import me.pinitnotification.application.push.PushService;
import me.pinitnotification.domain.notification.UpcomingScheduleNotification;
import me.pinitnotification.domain.notification.UpcomingScheduleNotificationRepository;
import me.pinitnotification.domain.push.PushSubscription;
import me.pinitnotification.domain.push.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchSchedulerTest {

    @Mock
    private UpcomingScheduleNotificationRepository notificationRepository;
    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;
    @Mock
    private PushService pushService;

    private NotificationDispatchScheduler scheduler;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2024-06-01T10:00:00Z"), ZoneOffset.UTC);
        scheduler = new NotificationDispatchScheduler(notificationRepository, pushSubscriptionRepository, pushService, clock);
    }

    @Test
    void dispatchDueNotifications_sendsAndDeletesPastNotifications() {
        UpcomingScheduleNotification past = new UpcomingScheduleNotification(1L, 10L, "title", "2024-06-01T09:50Z", "key-1");
        UpcomingScheduleNotification future = new UpcomingScheduleNotification(1L, 11L, "title2", "2024-06-01T10:30Z", "key-2");

        when(notificationRepository.findAll()).thenReturn(List.of(past, future));
        when(pushSubscriptionRepository.findAllByMemberId(1L))
                .thenReturn(List.of(new PushSubscription(1L, "device-1", "token-1"), new PushSubscription(1L, "device-2", "token-2")));

        scheduler.dispatchDueNotifications();

        verify(pushService).sendPushMessage("token-1", past);
        verify(pushService).sendPushMessage("token-2", past);
        verify(notificationRepository).deleteAllInBatch(List.of(past));
        verify(pushService, never()).sendPushMessage(anyString(), eq(future));
    }

    @Test
    void dispatchDueNotifications_deletesEvenWhenNoTokens() {
        UpcomingScheduleNotification past = new UpcomingScheduleNotification(2L, 20L, "title", "2024-06-01T09:00Z", "key-3");

        when(notificationRepository.findAll()).thenReturn(List.of(past));
        when(pushSubscriptionRepository.findAllByMemberId(2L)).thenReturn(List.of());

        scheduler.dispatchDueNotifications();

        verify(pushService, never()).sendPushMessage(anyString(), any());
        verify(notificationRepository).deleteAllInBatch(List.of(past));
    }
}
