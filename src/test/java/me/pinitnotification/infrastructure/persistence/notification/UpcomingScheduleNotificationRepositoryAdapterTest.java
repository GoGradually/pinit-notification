package me.pinitnotification.infrastructure.persistence.notification;

import me.pinitnotification.domain.notification.UpcomingScheduleNotification;
import me.pinitnotification.domain.notification.UpcomingScheduleNotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UpcomingScheduleNotificationRepositoryAdapter.class)
class UpcomingScheduleNotificationRepositoryAdapterTest {
    @Autowired
    private UpcomingScheduleNotificationRepository repository;

    @Test
    void savesAndLoadsDomainWithPublicId() {
        UUID publicId = UUID.randomUUID();
        UpcomingScheduleNotification created = new UpcomingScheduleNotification(
                publicId,
                1L,
                2L,
                "title",
                "2025-01-01T00:00:00Z",
                "key-1"
        );

        UpcomingScheduleNotification saved = repository.save(created);

        assertThat(saved.getLegacyId()).isNotNull();
        assertThat(saved.getId()).isEqualTo(publicId);

        Optional<UpcomingScheduleNotification> loaded =
                repository.findByScheduleIdAndOwnerId(2L, 1L);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getId()).isEqualTo(publicId);
    }
}
