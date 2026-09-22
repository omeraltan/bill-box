package com.billbox.auth;

import com.billbox.common.exception.ApiException;
import com.billbox.user.UserAccount;
import com.billbox.user.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final Duration TTL = Duration.ofMinutes(30);

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailer mailer;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(
            UserAccountRepository userAccountRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetMailer mailer
    ) {
        this.userAccountRepository = userAccountRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
    }

    @Transactional
    public void request(String email) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null || !user.isEnabled()) {
            return;
        }
        consumeOpenTokens(user);
        String rawToken = newToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(Instant.now().plus(TTL));
        tokenRepository.save(token);
        try {
            mailer.send(user.getEmail(), rawToken);
        } catch (RuntimeException ex) {
            log.error("Şifre sıfırlama iletisi gönderilemedi. email={}", user.getEmail(), ex);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "MAIL", "Sıfırlama iletisi gönderilemedi.");
        }
    }

    @Transactional
    public void reset(String rawToken, String password) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken.trim()))
                .orElseThrow(() -> ApiException.badRequest("Bağlantı geçersiz veya süresi dolmuş."));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("Bağlantı geçersiz veya süresi dolmuş.");
        }
        UserAccount user = token.getUser();
        if (!user.isEnabled()) {
            throw ApiException.badRequest("Bağlantı geçersiz veya süresi dolmuş.");
        }
        user.setPasswordHash(passwordEncoder.encode(password));
        userAccountRepository.save(user);
        consumeOpenTokens(user);
    }

    private void consumeOpenTokens(UserAccount user) {
        Instant now = Instant.now();
        tokenRepository.findByUser_IdAndUsedAtIsNull(user.getId()).forEach(open -> open.setUsedAt(now));
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
