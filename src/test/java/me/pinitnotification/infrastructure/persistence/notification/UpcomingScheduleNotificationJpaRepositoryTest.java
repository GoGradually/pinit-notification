package me.pinitnotification.infrastructure.persistence.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UpcomingScheduleNotificationJpaRepositoryTest {
    @Autowired
    private UpcomingScheduleNotificationJpaRepository repository;

    @Test
    void savesAndFindsByScheduleAndOwner() {
        UpcomingScheduleNotificationEntity entity = new UpcomingScheduleNotificationEntity();
        entity.setOwnerId(10L);
        entity.setScheduleId(20L);
        entity.setScheduleTitle("title");
        entity.setScheduleStartTime("2025-01-01T00:00:00Z");
        entity.setIdempotentKey("key-1");

        UpcomingScheduleNotificationEntity saved = repository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPublicId()).isNotNull();

        Optional<UpcomingScheduleNotificationEntity> loaded =
                repository.findByScheduleIdAndOwnerId(20L, 10L);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getPublicId()).isNotNull();
    }
}
