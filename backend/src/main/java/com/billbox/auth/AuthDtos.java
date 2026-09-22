package com.billbox.auth;

import com.billbox.common.enums.MembershipRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 160) String fullName,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @Size(max = 160) String organizationName
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record ForgotPasswordRequest(
            @NotBlank @Email @Size(max = 180) String email
    ) {
    }

    public record ForgotPasswordResponse(String message) {
    }

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {
    }

    public record AuthResponse(
            String token,
            UUID userId,
            UUID organizationId,
            String fullName,
            String email,
            String organizationName,
            MembershipRole role
    ) {
    }
}
