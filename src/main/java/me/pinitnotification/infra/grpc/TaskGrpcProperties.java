package me.pinitnotification.infra.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "task.grpc")
public record TaskGrpcProperties(String host, int port) {
}
