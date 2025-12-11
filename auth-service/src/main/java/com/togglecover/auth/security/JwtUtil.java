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
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

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
        // User ID is stored in the subject claim
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUsername(String token) {
        // Username (phone) is stored as a custom claim
        Claims claims = extractAllClaims(token);
        return claims.get("phone", String.class);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());       // Store user ID as custom claim
        claims.put("phone", user.getPhone());     // Store phone as custom claim
        claims.put("userType", user.getUserType().name());
        claims.put("fullName", user.getFullName());

        // Subject can be phone or user ID - choose one
        return createToken(claims, user.getPhone());  // Using phone as subject
    }

    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)  // Subject is phone number
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
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

    public User.UserType extractUserType(String token) {
        Claims claims = extractAllClaims(token);
        return User.UserType.valueOf(claims.get("userType", String.class));
    }
}