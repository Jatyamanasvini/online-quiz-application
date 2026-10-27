package com.example.quizplatform.security;

import com.example.quizplatform.domain.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiryMinutes;

    public JwtService(
            @Value("${app.jwt.secret:dev-only-quizora-secret-change-me-for-production-use-0123456789}") String secret,
            @Value("${app.jwt.expiry-minutes:480}") long expiryMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMinutes = expiryMinutes;
    }

    public String generate(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiryMinutes * 60)))
                .signWith(key)
                .compact();
    }

    /**
     * Parses and validates a token, throwing on any signature or expiry problem.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
