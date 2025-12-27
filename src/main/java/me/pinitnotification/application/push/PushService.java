package me.pinitnotification.application.push;

import me.pinitnotification.domain.notification.Notification;

public interface PushService {
    String getVapidPublicKey();

    void subscribe(Long memberId, String deviceId, String token);

    void unsubscribe(Long memberId, String deviceId, String token);
    void sendPushMessage(String token, Notification notification);

    boolean isSubscribed(Long memberId, String deviceId);
}
