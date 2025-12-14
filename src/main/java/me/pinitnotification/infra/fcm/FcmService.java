package me.pinitnotification.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.extern.slf4j.Slf4j;
import me.pinitnotification.application.push.PushService;
import me.pinitnotification.application.push.exception.PushSendFailedException;
import me.pinitnotification.domain.notification.Notification;
import me.pinitnotification.domain.push.PushSubscription;
import me.pinitnotification.domain.push.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class FcmService implements PushService {
    @Value("${vapid.keys.public}")
    private String vapidPublicKey;
    private final FirebaseMessaging firebaseMessaging;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public FcmService(FirebaseMessaging firebaseMessaging, PushSubscriptionRepository pushSubscriptionRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    @Override
    public void sendPushMessage(String token, Notification notification) {
        log.info("publish token: {}", token);
        Message message = Message.builder()
                .setToken(token)
                .putAllData(notification.getData())
                .build();
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error(e.getMessage(), e);
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                // Todo 토큰 삭제 방식 변경 필요
                pushSubscriptionRepository.deleteByToken(token);
            }
        }
    }

    @Override
    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    @Override
    @Transactional
    public void subscribe(Long memberId, String token) {
        pushSubscriptionRepository.save(new PushSubscription(memberId, token));
    }

    @Override
    @Transactional
    public void unsubscribe(Long memberId, String token) {
        pushSubscriptionRepository.delete(new PushSubscription(memberId, token));
    }
}
