package me.pinitnotification.interfaces.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "푸시 토큰 요청 바디")
public record PushTokenRequest(
        @Schema(description = "클라이언트에서 발급받은 FCM 푸시 토큰", example = "fcm-token-example")
        String token
) {
}
