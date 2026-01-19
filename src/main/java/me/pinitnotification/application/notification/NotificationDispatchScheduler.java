package me.pinitnotification.application.notification;

import me.pinitnotification.application.push.PushSendResult;
import me.pinitnotification.application.push.PushService;
import me.pinitnotification.domain.notification.UpcomingScheduleNotification;
import me.pinitnotification.domain.notification.UpcomingScheduleNotificationRepository;
import me.pinitnotification.domain.push.PushSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificationDispatchScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchScheduler.class);

    private final UpcomingScheduleNotificationRepository notificationRepository;
    private final NotificationDispatchQueryRepository dispatchQueryRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushService pushService;
    private final Clock clock;

    public NotificationDispatchScheduler(UpcomingScheduleNotificationRepository notificationRepository,
                                         NotificationDispatchQueryRepository dispatchQueryRepository,
                                         PushSubscriptionRepository pushSubscriptionRepository,
                                         PushService pushService,
                                         Clock clock) {
        this.notificationRepository = notificationRepository;
        this.dispatchQueryRepository = dispatchQueryRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.pushService = pushService;
        this.clock = clock;
    }

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void dispatchDueNotifications() {
        Instant now = Instant.now(clock);
        List<NotificationDispatchItem> dispatchItems = dispatchQueryRepository.findDueNotificationsWithTokens(now);

        if (dispatchItems.isEmpty()) {
            return;
        }

        Set<String> tokensToDelete = new HashSet<>();
        dispatchItems.forEach(item -> sendNotificationToOwner(item, tokensToDelete));
        notificationRepository.deleteAllInBatch(dispatchItems.stream().map(NotificationDispatchItem::notification).toList());

        if (!tokensToDelete.isEmpty()) {
            pushSubscriptionRepository.deleteByTokens(tokensToDelete);
        }
    }


    private void sendNotificationToOwner(NotificationDispatchItem dispatchItem, Set<String> tokensToDelete) {
        UpcomingScheduleNotification notification = dispatchItem.notification();
        List<String> tokens = dispatchItem.tokens();

        if (tokens.isEmpty()) {
            log.info("No push tokens for owner; skip sending. ownerId={}, scheduleId={}", notification.getOwnerId(), notification.getScheduleId());
            return;
        }

        tokens.forEach(token -> {
            PushSendResult result = pushService.sendPushMessage(token, notification);
            if (result.shouldDeleteToken()) {
                tokensToDelete.add(token);
            }
        });
    }
}
