package com.togglecover.user.config;

import com.togglecover.user.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/favicon.ico",
                                "/h2-console/**"
                        ).permitAll()

                        // User endpoints
                        .requestMatchers("/api/v1/users/profile").authenticated()
                        .requestMatchers("/api/v1/users/preferences").authenticated()
                        .requestMatchers("/api/v1/users/profile/picture").authenticated()

                        // Admin only endpoints
                        .requestMatchers("/api/v1/users").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**/verify").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/search").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/bulk").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**/deactivate").hasRole("ADMIN")

                        // User-specific endpoints - use method security instead
                        .requestMatchers("/api/v1/users/{userId}").authenticated()
                        .requestMatchers("/api/v1/users/{userId}/stats").authenticated()
                        .requestMatchers("/api/v1/users/{userId}/activity").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}