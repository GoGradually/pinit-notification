package me.pinitnotification.infrastructure.persistence.push;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionJpaRepository extends JpaRepository<PushSubscriptionEntity, Long> {
    Optional<PushSubscriptionEntity> findByMemberIdAndDeviceId(Long memberId, String deviceId);

    List<PushSubscriptionEntity> findAllByMemberId(Long memberId);

    void deleteByToken(String token);

    void deleteByMemberIdAndDeviceId(Long memberId, String deviceId);
}
