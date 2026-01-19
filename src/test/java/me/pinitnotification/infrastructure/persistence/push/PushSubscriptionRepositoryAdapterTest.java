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
    @Autowired
    private PushSubscriptionJpaRepository jpaRepository;

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

        assertThat(saved.getId()).isEqualTo(publicId);

        Optional<PushSubscription> loaded =
                repository.findByMemberIdAndDeviceId(101L, "device-1");

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getId()).isEqualTo(publicId);
    }

    @Test
    void deletesTokensInBatch() {
        PushSubscriptionEntity token1 = subscription("token-1", "device-1");
        PushSubscriptionEntity token2 = subscription("token-2", "device-2");
        PushSubscriptionEntity token3 = subscription("token-3", "device-3");
        jpaRepository.save(token1);
        jpaRepository.save(token2);
        jpaRepository.save(token3);

        repository.deleteByTokens(java.util.List.of("token-1", "token-x"));

        assertThat(jpaRepository.findAll())
                .extracting(PushSubscriptionEntity::getToken)
                .containsExactlyInAnyOrder("token-2", "token-3");
    }

    private PushSubscriptionEntity subscription(String token, String deviceId) {
        PushSubscriptionEntity entity = new PushSubscriptionEntity();
        entity.setPublicId(UUID.randomUUID());
        entity.setMemberId(201L);
        entity.setDeviceId(deviceId);
        entity.setToken(token);
        return entity;
    }
}
