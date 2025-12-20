package me.pinitnotification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import me.pinitnotification.infrastructure.authenticate.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@ActiveProfiles("test")
@SpringBootTest
class PinitNotificationApplicationTests {

    @MockitoBean
    FirebaseApp firebaseApp;
    @MockitoBean
    FirebaseMessaging firebaseMessaging;
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;
    @Test
    void contextLoads() {
    }


}
