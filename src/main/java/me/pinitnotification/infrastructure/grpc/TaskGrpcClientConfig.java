package me.pinitnotification.infrastructure.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import me.gg.pinit.pinittask.grpc.ScheduleGrpcServiceGrpc;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TaskGrpcProperties.class)
public class TaskGrpcClientConfig {

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel scheduleManagedChannel(TaskGrpcProperties properties) {
        return ManagedChannelBuilder.forAddress(properties.host(), properties.port())
                .usePlaintext()
                .build();
    }

    @Bean
    public ScheduleGrpcServiceGrpc.ScheduleGrpcServiceBlockingStub scheduleGrpcServiceBlockingStub(ManagedChannel channel) {
        return ScheduleGrpcServiceGrpc.newBlockingStub(channel);
    }
}
