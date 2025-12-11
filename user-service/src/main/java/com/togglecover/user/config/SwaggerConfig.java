package com.togglecover.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI toggleCoverOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Toggle Cover - User Service API")
                        .description("User profile management service for Toggle Cover platform")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Toggle Cover Team")
                                .email("contact@togglecover.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://togglecover.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}