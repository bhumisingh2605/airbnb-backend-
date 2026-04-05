package com.codingshuttle.projects.airBnbApp.security;

import com.codingshuttle.projects.airBnbApp.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Generate Access Token (10 minutes)
    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", user.getRoles())
                .issuedAt(new Date(now))
                .expiration(new Date(now + 10 * 60 * 1000L)) // 10 minutes
                .signWith(getSecretKey())                    // Modern way (algorithm inferred)
                .compact();
    }

    // Generate Refresh Token (60 days)
    public String generateRefreshToken(User user) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date(now))
                .expiration(new Date(now + 60L * 24 * 60 * 60 * 1000)) // 60 days
                .signWith(getSecretKey())
                .compact();
    }

    // Private helper to parse token
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract username (email)
    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    // Check if token is valid for a specific user
    public boolean isTokenValid(String token, User user) {
        String username = extractUsername(token);
        return username.equals(user.getEmail()) && !isTokenExpired(token);
    }

    // Overloaded version for UserDetails (more flexible)
    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    // Simple token validation (used in filter)
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}