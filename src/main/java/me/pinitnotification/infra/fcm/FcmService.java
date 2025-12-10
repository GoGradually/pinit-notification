package me.pinitnotification.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import me.pinitnotification.application.push.PushService;
import me.pinitnotification.application.push.exception.PushSendFailedException;
import me.pinitnotification.domain.PushSubscription;
import me.pinitnotification.domain.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void sendPushMessage(String token, String title, String body) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification).build();

        try{
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new PushSendFailedException(e);
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
