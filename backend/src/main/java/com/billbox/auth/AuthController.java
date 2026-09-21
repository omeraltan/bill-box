package com.billbox.auth;

import com.billbox.auth.AuthDtos.AuthResponse;
import com.billbox.auth.AuthDtos.LoginRequest;
import com.billbox.auth.AuthDtos.RegisterRequest;
import com.billbox.security.AuthPrincipal;
import com.billbox.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me() {
        AuthPrincipal principal = SecurityUtils.current();
        return authService.me(principal);
    }
}
