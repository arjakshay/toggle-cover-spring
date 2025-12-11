package com.togglecover.user.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Content-Type", "application/json");
            template.header("Accept", "application/json");
            // Add a special header for service-to-service calls
            template.header("X-Internal-Request", "true");
        };
    }
}