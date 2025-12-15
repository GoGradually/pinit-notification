package me.pinitnotification.infrastructure.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import me.gg.pinit.pinittask.grpc.GetScheduleRequest;
import me.gg.pinit.pinittask.grpc.GetScheduleResponse;
import me.gg.pinit.pinittask.grpc.ScheduleGrpcServiceGrpc;
import me.pinitnotification.application.notification.query.ScheduleBasics;
import me.pinitnotification.application.notification.query.ScheduleQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ScheduleGrpcClient implements ScheduleQueryPort {
    private static final Logger log = LoggerFactory.getLogger(ScheduleGrpcClient.class);
    private final ScheduleGrpcServiceGrpc.ScheduleGrpcServiceBlockingStub scheduleStub;

    public ScheduleGrpcClient(ScheduleGrpcServiceGrpc.ScheduleGrpcServiceBlockingStub scheduleStub) {
        this.scheduleStub = scheduleStub;
    }

    @Override
    public ScheduleBasics getScheduleBasics(Long scheduleId, Long ownerId) {
        try {
            GetScheduleResponse response = scheduleStub.getScheduleBasics(
                    GetScheduleRequest.newBuilder()
                            .setScheduleId(scheduleId)
                            .setOwnerId(ownerId)
                            .build()
            );

            return new ScheduleBasics(
                    response.getScheduleId(),
                    response.getOwnerId(),
                    response.getScheduleTitle(),
                    toIsoString(response.getDesignatedStartTime())
            );
        } catch (StatusRuntimeException e) {
            log.error("Failed to fetch schedule basics via gRPC. scheduleId={}, ownerId={}", scheduleId, ownerId, e);
            throw e;
        }
    }

    private String toIsoString(Timestamp timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return instant.toString();
    }
}
