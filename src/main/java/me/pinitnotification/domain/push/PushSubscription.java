package me.pinitnotification.domain.push;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_deviceId_memberId",
                columnNames = {"member_id", "device_id"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
public class PushSubscription {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    @Column(name = "token", nullable = false)
    private String token;
    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;
    protected PushSubscription() {}

    public PushSubscription(Long memberId, String deviceId, String token) {
        this.memberId = memberId;
        this.deviceId = deviceId;
        this.token = token;
    }

    public void updateToken(String token) {
        if (this.modifiedAt.isAfter(Instant.now())) {
            return;
        }
        this.token = token;
    }
}
