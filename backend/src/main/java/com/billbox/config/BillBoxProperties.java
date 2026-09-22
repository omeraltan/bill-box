package com.billbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "billbox")
public record BillBoxProperties(
        Jwt jwt,
        Storage storage,
        Cors cors,
        String publicUrl,
        Mail mail
) {
    public record Jwt(String secret, long expirationMs) {
    }

    public record Storage(String root) {
    }

    public record Cors(String allowedOrigins) {
    }

    public record Mail(String from) {
    }
}
