package me.pinitnotification.domain.push;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    Optional<PushSubscription> findByMemberIdAndDeviceId(Long memberId, String deviceId);

    List<PushSubscription> findAllByMemberId(Long memberId);

    void deleteByToken(String token);

    void deleteByMemberIdAndDeviceId(Long memberId, String deviceId);
}
