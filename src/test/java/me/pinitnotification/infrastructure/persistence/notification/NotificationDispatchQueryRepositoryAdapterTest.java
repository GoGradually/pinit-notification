package me.pinitnotification.infrastructure.persistence.notification;

import jakarta.persistence.EntityManager;
import me.pinitnotification.application.notification.NotificationDispatchItem;
import me.pinitnotification.application.notification.NotificationDispatchQueryRepository;
import me.pinitnotification.domain.push.PushSubscription;
import me.pinitnotification.domain.push.PushSubscriptionRepository;
import me.pinitnotification.infrastructure.persistence.push.PushSubscriptionRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({NotificationDispatchQueryRepositoryAdapter.class, PushSubscriptionRepositoryAdapter.class})
class NotificationDispatchQueryRepositoryAdapterTest {

    @Autowired
    private NotificationDispatchQueryRepository repository;
    @Autowired
    private UpcomingScheduleNotificationJpaRepository notificationJpaRepository;
    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void returnsDueNotificationsWithAggregatedTokens() {
        // given
        UpcomingScheduleNotificationEntity dueWithTokens = notificationJpaRepository.save(notification(1L, 11L, "2024-06-01T09:50:00Z"));
        notificationJpaRepository.save(notification(2L, 12L, "2024-06-01T09:55:00Z")); // no tokens
        notificationJpaRepository.save(notification(3L, 13L, "2024-06-01T10:30:00Z")); // future, should be excluded

        pushSubscriptionRepository.save(subscription(1L, "device-1", "token-1"));
        pushSubscriptionRepository.save(subscription(1L, "device-2", "token-2"));

        entityManager.flush();
        entityManager.clear();

        // when
        List<NotificationDispatchItem> results = repository.findAllDueNotificationsWithTokens(Instant.parse("2024-06-01T10:00:00Z"));

        // then
        assertThat(results)
                .hasSize(2)
                .extracting(item -> item.notification().getScheduleId())
                .containsExactlyInAnyOrder(11L, 12L);

        NotificationDispatchItem withTokens = results.stream()
                .filter(item -> item.notification().getScheduleId().equals(11L))
                .findFirst()
                .orElseThrow();
        assertThat(withTokens.tokens()).containsExactlyInAnyOrder("token-1", "token-2");

        NotificationDispatchItem withoutTokens = results.stream()
                .filter(item -> item.notification().getScheduleId().equals(12L))
                .findFirst()
                .orElseThrow();
        assertThat(withoutTokens.tokens()).isEmpty();
    }

    private UpcomingScheduleNotificationEntity notification(Long ownerId, Long scheduleId, String startTimeIso) {
        UpcomingScheduleNotificationEntity entity = new UpcomingScheduleNotificationEntity();
        entity.setPublicId(UUID.randomUUID());
        entity.setOwnerId(ownerId);
        entity.setScheduleId(scheduleId);
        entity.setScheduleTitle("title-" + scheduleId);
        entity.setScheduleStartTime(startTimeIso);
        entity.setIdempotentKey("key-" + scheduleId);
        return entity;
    }

    private PushSubscription subscription(Long memberId, String deviceId, String token) {
        return new PushSubscription(UUID.randomUUID(), memberId, deviceId, token);
    }
}
