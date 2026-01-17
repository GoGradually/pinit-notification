package me.pinitnotification.domain.push;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class PushSubscription {
    private Long legacyId;
    private UUID id;
    private Long memberId;
    private String deviceId;
    private String token;
    private Instant modifiedAt;
    protected PushSubscription() {}

    public PushSubscription(Long legacyId, UUID id, Long memberId, String deviceId, String token, Instant modifiedAt) {
        this.legacyId = legacyId;
        this.id = id;
        this.memberId = memberId;
        this.deviceId = deviceId;
        this.token = token;
        this.modifiedAt = modifiedAt;
    }

    public PushSubscription(UUID id, Long memberId, String deviceId, String token) {
        this.id = id;
        this.memberId = memberId;
        this.deviceId = deviceId;
        this.token = token;
    }

    public PushSubscription(Long memberId, String deviceId, String token) {
        this.memberId = memberId;
        this.deviceId = deviceId;
        this.token = token;
    }

    public void updateToken(String token) {
        if (this.modifiedAt != null && this.modifiedAt.isAfter(Instant.now())) {
            return;
        }
        this.token = token;
    }
}
