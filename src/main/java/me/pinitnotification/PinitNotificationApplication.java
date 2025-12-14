package me.pinitnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PinitNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PinitNotificationApplication.class, args);
    }

}
