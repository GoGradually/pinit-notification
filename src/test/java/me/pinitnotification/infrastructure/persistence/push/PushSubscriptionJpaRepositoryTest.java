package me.pinitnotification.infrastructure.persistence.push;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PushSubscriptionJpaRepositoryTest {
    @Autowired
    private PushSubscriptionJpaRepository repository;

    @Test
    void savesAndFindsByMemberAndDevice() {
        PushSubscriptionEntity entity = new PushSubscriptionEntity();
        entity.setMemberId(101L);
        entity.setDeviceId("device-1");
        entity.setToken("token-1");

        PushSubscriptionEntity saved = repository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPublicId()).isNotNull();
        assertThat(saved.getModifiedAt()).isNotNull();

        Optional<PushSubscriptionEntity> loaded =
                repository.findByMemberIdAndDeviceId(101L, "device-1");

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getModifiedAt()).isNotNull();
    }

}
