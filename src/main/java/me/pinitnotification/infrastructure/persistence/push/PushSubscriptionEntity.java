package me.pinitnotification.infrastructure.persistence.push;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.pinitnotification.infrastructure.persistence.UuidV7Generator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "push_subscription",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_deviceId_memberId",
                        columnNames = {"member_id", "device_id"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class PushSubscriptionEntity {
    private Long id;

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "public_id", length = 36)
    private UUID publicId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    @Column(name = "token", nullable = false)
    private String token;
    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    protected PushSubscriptionEntity() {
    }

    @PrePersist
    protected void assignPublicId() {
        if (publicId == null) {
            publicId = UuidV7Generator.generate();
        }
    }
}
