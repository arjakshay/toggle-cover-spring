package com.togglecover.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "msg91")
public class Msg91Config {
    private String authKey;
    private String senderId;
    private String route;
    private String country;
    private String baseUrl = "https://api.msg91.com/api";
    private boolean enabled = true;
    private boolean demoMode = true;
}