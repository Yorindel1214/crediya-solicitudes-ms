package co.com.crediya.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI crediyaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Crediya - Microservicio de Solicitudes")
                .description("API para registrar solicitudes de préstamo")
                .version("1.0.0"));
    }
}
