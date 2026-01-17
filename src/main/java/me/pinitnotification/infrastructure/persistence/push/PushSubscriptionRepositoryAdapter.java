package me.pinitnotification.infrastructure.persistence.push;

import me.pinitnotification.domain.push.PushSubscription;
import me.pinitnotification.domain.push.PushSubscriptionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PushSubscriptionRepositoryAdapter implements PushSubscriptionRepository {
    private final PushSubscriptionJpaRepository jpaRepository;

    public PushSubscriptionRepositoryAdapter(PushSubscriptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PushSubscription> findByMemberIdAndDeviceId(Long memberId, String deviceId) {
        return jpaRepository.findByMemberIdAndDeviceId(memberId, deviceId)
                .map(this::toDomain);
    }

    @Override
    public List<PushSubscription> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberId(memberId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PushSubscription save(PushSubscription subscription) {
        PushSubscriptionEntity saved = jpaRepository.save(toEntity(subscription));
        return toDomain(saved);
    }

    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteByToken(token);
    }

    @Override
    public void deleteByMemberIdAndDeviceId(Long memberId, String deviceId) {
        jpaRepository.deleteByMemberIdAndDeviceId(memberId, deviceId);
    }

    private PushSubscription toDomain(PushSubscriptionEntity entity) {
        return new PushSubscription(
                entity.getId(),
                entity.getPublicId(),
                entity.getMemberId(),
                entity.getDeviceId(),
                entity.getToken(),
                entity.getModifiedAt()
        );
    }

    private PushSubscriptionEntity toEntity(PushSubscription domain) {
        PushSubscriptionEntity entity = new PushSubscriptionEntity();
        entity.setId(domain.getLegacyId());
        entity.setPublicId(domain.getId());
        entity.setMemberId(domain.getMemberId());
        entity.setDeviceId(domain.getDeviceId());
        entity.setToken(domain.getToken());
        entity.setModifiedAt(domain.getModifiedAt());
        return entity;
    }
}
