package com.billbox.auth;

import com.billbox.auth.AuthDtos.AuthResponse;
 import com.billbox.auth.AuthDtos.ForgotPasswordRequest;
import com.billbox.auth.AuthDtos.ForgotPasswordResponse;
import com.billbox.auth.AuthDtos.LoginRequest;
import com.billbox.auth.AuthDtos.RegisterRequest;
import com.billbox.auth.AuthDtos.ResetPasswordRequest;
import com.billbox.security.AuthPrincipal;
import com.billbox.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.request(request.email());
        return new ForgotPasswordResponse("Kayıtlı bir hesapsa sıfırlama bağlantısı gönderildi.");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reset(request.token(), request.password());
    }

    @GetMapping("/me")
    public AuthResponse me() {
        AuthPrincipal principal = SecurityUtils.current();
        return authService.me(principal);
    }
}
