package com.billbox.file;

import com.billbox.config.BillBoxProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(BillBoxProperties properties) throws IOException {
        this.root = Path.of(properties.storage().root()).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public String store(UUID organizationId, UUID ownerId, MultipartFile file) throws IOException {
        return store(organizationId, "invoices", ownerId, file);
    }

    public String store(UUID organizationId, String area, UUID ownerId, MultipartFile file) throws IOException {
        String safeName = sanitize(file.getOriginalFilename());
        String key = organizationId + "/" + area + "/" + ownerId + "/" + UUID.randomUUID() + "-" + safeName;
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("Geçersiz dosya yolu.");
        }
        Files.createDirectories(target.getParent());
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target);
        }
        return key;
    }

    public Path resolve(String storageKey) {
        return root.resolve(storageKey).normalize();
    }

    public void delete(String storageKey) throws IOException {
        Path target = resolve(storageKey);
        if (target.startsWith(root)) {
            Files.deleteIfExists(target);
        }
    }

    private String sanitize(String name) {
        String value = name == null || name.isBlank() ? "belge" : name;
        return value.replaceAll("[^a-zA-Z0-9._\\-ğüşöçıİĞÜŞÖÇ ]", "_");
    }
}
