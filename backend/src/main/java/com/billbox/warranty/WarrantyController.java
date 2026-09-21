package com.billbox.warranty;

import com.billbox.common.web.PageResponse;
import com.billbox.security.SecurityUtils;
import com.billbox.warranty.WarrantyDtos.DetailResponse;
import com.billbox.warranty.WarrantyDtos.SummaryResponse;
import com.billbox.warranty.WarrantyDtos.UpsertRequest;
import com.billbox.warranty.WarrantyService.StoredFile;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/warranties")
public class WarrantyController {

    private final WarrantyService warrantyService;

    public WarrantyController(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    @GetMapping
    public PageResponse<SummaryResponse> search(
            @RequestParam(required = false) CoverageStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return warrantyService.search(SecurityUtils.current(), status, page, size);
    }

    @GetMapping("/{id}")
    public DetailResponse get(@PathVariable UUID id) {
        return warrantyService.get(SecurityUtils.current(), id);
    }

    @PostMapping
    public DetailResponse create(@Valid @RequestBody UpsertRequest request) {
        return warrantyService.create(SecurityUtils.current(), request);
    }

    @PostMapping("/from-receipt")
    public DetailResponse createFromReceipt(@RequestBody Map<String, String> body) {
        int months = Integer.parseInt(body.getOrDefault("months", "24"));
        return warrantyService.createFromReceiptItem(
                SecurityUtils.current(),
                UUID.fromString(body.get("receiptId")),
                UUID.fromString(body.get("itemId")),
                months
        );
    }

    @PutMapping("/{id}")
    public DetailResponse update(@PathVariable UUID id, @Valid @RequestBody UpsertRequest request) {
        return warrantyService.update(SecurityUtils.current(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        warrantyService.delete(SecurityUtils.current(), id);
    }

    @PostMapping(path = "/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DetailResponse attach(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return warrantyService.attach(SecurityUtils.current(), id, file);
    }

    @GetMapping("/{id}/files/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID id, @PathVariable UUID fileId) {
        StoredFile file = warrantyService.download(SecurityUtils.current(), id, fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.name(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.bytes());
    }
}
