package com.billbox.auth;

import com.billbox.config.BillBoxProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class PasswordResetMailer {

    private static final String HTML_TEMPLATE = "mail/password-reset.html";
    private static final String TEXT_TEMPLATE = "mail/password-reset.txt";

    private final JavaMailSender mailSender;
    private final BillBoxProperties properties;

    public PasswordResetMailer(JavaMailSender mailSender, BillBoxProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void send(String email, String rawToken) {
        String link = publicBase() + "/sifre-yenile?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.mail().from());
            helper.setTo(email);
            helper.setSubject("Bill Box — şifrenizi yenileyin");
            helper.setText(render(TEXT_TEMPLATE, link), render(HTML_TEMPLATE, escapeHtml(link)));
            mailSender.send(message);
        } catch (MessagingException | MailException | IOException ex) {
            throw new IllegalStateException("Şifre sıfırlama iletisi gönderilemedi.", ex);
        }
    }

    private String publicBase() {
        String url = properties.publicUrl();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String render(String classpath, String link) throws IOException {
        try (InputStream input = new ClassPathResource(classpath).getInputStream()) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("{{resetUrl}}", link);
        }
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
