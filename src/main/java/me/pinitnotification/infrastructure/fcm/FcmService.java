package me.pinitnotification.infrastructure.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.extern.slf4j.Slf4j;
import me.pinitnotification.application.push.PushSendResult;
import me.pinitnotification.application.push.PushService;
import me.pinitnotification.domain.notification.Notification;
import me.pinitnotification.domain.push.PushSubscription;
import me.pinitnotification.domain.push.PushSubscriptionRepository;
import me.pinitnotification.domain.shared.IdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class FcmService implements PushService {
    @Value("${vapid.keys.public}")
    private String vapidPublicKey;
    private final FirebaseMessaging firebaseMessaging;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final IdGenerator idGenerator;

    public FcmService(FirebaseMessaging firebaseMessaging,
                      PushSubscriptionRepository pushSubscriptionRepository,
                      IdGenerator idGenerator) {
        this.firebaseMessaging = firebaseMessaging;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public PushSendResult sendPushMessage(String token, Notification notification) {
        log.info("publish token: {}", token);
        Message message = Message.builder()
                .setToken(token)
                .putAllData(notification.getData())
                .build();
        try {
            firebaseMessaging.send(message);
            return PushSendResult.successResult();
        } catch (FirebaseMessagingException e) {
            log.error(e.getMessage(), e);
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                return PushSendResult.invalidTokenResult();
            }
            return PushSendResult.failedResult();
        }
    }

    @Override
    public boolean isSubscribed(Long memberId, String deviceId) {
        return pushSubscriptionRepository.findByMemberIdAndDeviceId(memberId, deviceId).isPresent();
    }

    @Override
    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    @Override
    @Transactional
    public void subscribe(Long memberId, String deviceId, String token) {
        Optional<PushSubscription> byMemberIdAndDeviceId = pushSubscriptionRepository.findByMemberIdAndDeviceId(memberId, deviceId);
        if (byMemberIdAndDeviceId.isPresent()) {
            PushSubscription existingSubscription = byMemberIdAndDeviceId.get();
            existingSubscription.updateToken(token);
        } else {
            pushSubscriptionRepository.save(new PushSubscription(idGenerator.generate(), memberId, deviceId, token));
        }
    }

    @Override
    @Transactional
    public void unsubscribe(Long memberId, String deviceId, String token) {
        pushSubscriptionRepository.deleteByMemberIdAndDeviceId(memberId, deviceId);
    }
}
