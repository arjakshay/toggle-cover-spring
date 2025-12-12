package com.togglecover.auth.security;

import com.togglecover.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh.expiration:2592000000}")
    private Long refreshTokenExpiration;

    // Original method (for backward compatibility)
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("phone", user.getPhone());
        claims.put("userType", user.getUserType().name());
        claims.put("fullName", user.getFullName());

        return createToken(claims, user.getId(), accessTokenExpiration);
    }

    // New method with session support
    public String generateToken(User user, String sessionId, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("phone", user.getPhone());
        claims.put("userType", user.getUserType().name());
        claims.put("fullName", user.getFullName());
        claims.put("sessionId", sessionId);
        claims.put("deviceId", deviceId != null ? deviceId : "unknown");
        claims.put("tokenType", "ACCESS");

        return createToken(claims, user.getId(), accessTokenExpiration);
    }

    public String generateRefreshToken(User user, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("sessionId", sessionId);
        claims.put("tokenType", "REFRESH");
        claims.put("jti", UUID.randomUUID().toString());

        return createToken(claims, user.getId(), refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("phone", String.class);
    }

    public String extractSessionId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("sessionId", String.class);
    }

    public String extractDeviceId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("deviceId", String.class);
    }

    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("tokenType", String.class);
    }

    public User.UserType extractUserType(String token) {
        Claims claims = extractAllClaims(token);
        String userTypeStr = claims.get("userType", String.class);
        return User.UserType.valueOf(userTypeStr);
    }

    public Boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> debugToken(String token) {
        Claims claims = extractAllClaims(token);
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("subject", claims.getSubject());
        debugInfo.put("allClaims", claims);
        return debugInfo;
    }
}