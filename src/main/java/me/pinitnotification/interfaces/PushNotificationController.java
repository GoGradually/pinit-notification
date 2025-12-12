package me.pinitnotification.interfaces;

import me.pinitnotification.application.push.PushService;
import me.pinitnotification.domain.member.MemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/push")
@Tag(name = "푸시 알림", description = "푸시 구독 및 VAPID 키 관련 API")
public class PushNotificationController {
    private final PushService pushService;

    public PushNotificationController(PushService pushService) {
        this.pushService = pushService;
    }

    @GetMapping("/vapid")
    @Operation(
            summary = "VAPID 공개키 조회",
            description = "웹 푸시 구독에 사용되는 VAPID 공개키를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "VAPID 공개키",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    public String getVapidPublicKey() {
        return pushService.getVapidPublicKey();
    }

    @PostMapping("/subscribe")
    @Operation(
            summary = "푸시 토큰 구독 등록",
            description = "인증된 회원의 푸시 토큰을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 등록 완료"),
            @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않음", content = @Content)
    })
    public void subscribe(
            @Parameter(hidden = true) @MemberId Long memberId,
            @Parameter(description = "클라이언트에서 발급받은 FCM 푸시 토큰", required = true)
            @RequestParam String token) {
        pushService.subscribe(memberId, token);
    }

    @PostMapping("/unsubscribe")
    @Operation(
            summary = "푸시 토큰 구독 해지",
            description = "인증된 회원의 등록된 푸시 토큰을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 해지 완료"),
            @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않음", content = @Content)
    })
    public void unsubscribe(
            @Parameter(hidden = true) @MemberId Long memberId,
            @Parameter(description = "클라이언트에서 발급받은 FCM 푸시 토큰", required = true)
            @RequestParam String token) {
        pushService.unsubscribe(memberId, token);
    }
}
