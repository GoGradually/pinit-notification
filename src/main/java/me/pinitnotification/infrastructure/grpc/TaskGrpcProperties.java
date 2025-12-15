package me.pinitnotification.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "task.grpc")
public record TaskGrpcProperties(String host, int port) {
}
