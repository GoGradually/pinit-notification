package me.pinitnotification.interfaces.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Server production = new Server()
                .url("https://notification.pinit.go-gradually.me")
                .description("Production");

        return new OpenAPI()
                .servers(List.of(production));
    }
}
