package me.pinitnotification.domain.push;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository {
    Optional<PushSubscription> findByMemberIdAndDeviceId(Long memberId, String deviceId);

    List<PushSubscription> findAllByMemberId(Long memberId);

    PushSubscription save(PushSubscription subscription);

    void deleteByToken(String token);

    void deleteByTokens(Collection<String> tokens);

    void deleteByMemberIdAndDeviceId(Long memberId, String deviceId);
}
