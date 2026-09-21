package com.billbox.security;

import com.billbox.config.BillBoxProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(BillBoxProperties properties) {
        this.key = Keys.hmacShaKeyFor(padSecret(properties.jwt().secret()).getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.jwt().expirationMs();
    }

    public String createToken(AuthPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.userId().toString())
                .claim("org", principal.organizationId().toString())
                .claim("email", principal.email())
                .claim("name", principal.fullName())
                .claim("orgName", principal.organizationName())
                .claim("role", principal.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID userId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public UUID organizationId(Claims claims) {
        return UUID.fromString(claims.get("org", String.class));
    }

    private String padSecret(String secret) {
        if (secret.length() >= 32) {
            return secret;
        }
        return secret + "0".repeat(32 - secret.length());
    }
}
