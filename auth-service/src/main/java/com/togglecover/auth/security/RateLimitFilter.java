package com.togglecover.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();
        String key = "ratelimit:" + clientIp + ":" + endpoint;

        // Check rate limit
        Long current = redisTemplate.opsForValue().increment(key, 1);

        if (current == 1) {
            // First request, set expiry
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        // Set rate limit headers
        response.setHeader("X-Rate-Limit-Limit", "100");
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(Math.max(0, 100 - current)));
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(60)); // 60 seconds

        if (current > 100) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Try again later.");
            log.warn("Rate limit exceeded for IP: {}, Endpoint: {}", clientIp, endpoint);
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}