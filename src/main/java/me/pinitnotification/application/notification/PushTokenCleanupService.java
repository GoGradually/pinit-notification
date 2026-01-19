package me.pinitnotification.application.notification;

import me.pinitnotification.domain.push.PushSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class PushTokenCleanupService {
    private static final Logger log = LoggerFactory.getLogger(PushTokenCleanupService.class);
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public PushTokenCleanupService(PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteTokensInNewTransaction(Set<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        pushSubscriptionRepository.deleteByTokens(tokens);
        log.info("Deleted {} invalid push tokens", tokens.size());
    }
}
