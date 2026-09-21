package com.billbox.receipt;

import com.billbox.common.web.PageResponse;
import com.billbox.receipt.ReceiptDtos.DetailResponse;
import com.billbox.receipt.ReceiptDtos.SummaryResponse;
import com.billbox.receipt.ReceiptDtos.UpsertRequest;
import com.billbox.receipt.ReceiptService.StoredFile;
import com.billbox.security.SecurityUtils;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public PageResponse<SummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return receiptService.search(SecurityUtils.current(), q, page, size);
    }

    @GetMapping("/{id}")
    public DetailResponse get(@PathVariable UUID id) {
        return receiptService.get(SecurityUtils.current(), id);
    }

    @PostMapping
    public DetailResponse create(@Valid @RequestBody UpsertRequest request) {
        return receiptService.create(SecurityUtils.current(), request);
    }

    @PutMapping("/{id}")
    public DetailResponse update(@PathVariable UUID id, @Valid @RequestBody UpsertRequest request) {
        return receiptService.update(SecurityUtils.current(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        receiptService.delete(SecurityUtils.current(), id);
    }

    @PostMapping(path = "/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DetailResponse attach(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return receiptService.attach(SecurityUtils.current(), id, file);
    }

    @GetMapping("/{id}/files/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID id, @PathVariable UUID fileId) {
        StoredFile file = receiptService.download(SecurityUtils.current(), id, fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.name(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.bytes());
    }
}
