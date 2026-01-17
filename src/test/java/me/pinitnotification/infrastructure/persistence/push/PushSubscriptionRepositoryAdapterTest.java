package me.pinitnotification.infrastructure.persistence.push;

import me.pinitnotification.domain.push.PushSubscription;
import me.pinitnotification.domain.push.PushSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PushSubscriptionRepositoryAdapter.class)
class PushSubscriptionRepositoryAdapterTest {
    @Autowired
    private PushSubscriptionRepository repository;

    @Test
    void savesAndLoadsDomainWithPublicId() {
        UUID publicId = UUID.randomUUID();
        PushSubscription created = new PushSubscription(
                publicId,
                101L,
                "device-1",
                "token-1"
        );

        PushSubscription saved = repository.save(created);

        assertThat(saved.getLegacyId()).isNotNull();
        assertThat(saved.getId()).isEqualTo(publicId);

        Optional<PushSubscription> loaded =
                repository.findByMemberIdAndDeviceId(101L, "device-1");

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getId()).isEqualTo(publicId);
    }
}
