package com.billbox.invoice;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.InvoiceStatus;
import com.billbox.common.web.PageResponse;
import com.billbox.invoice.InvoiceDtos.DetailResponse;
import com.billbox.invoice.InvoiceDtos.SummaryResponse;
import com.billbox.invoice.InvoiceDtos.UpsertRequest;
import com.billbox.invoice.InvoiceService.FileDownload;
import com.billbox.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public PageResponse<SummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) InvoiceDirection direction,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) UUID vendorId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return invoiceService.search(SecurityUtils.current(), q, direction, status, vendorId, categoryId, from, to, page, size);
    }

    @GetMapping("/{id}")
    public DetailResponse get(@PathVariable UUID id) {
        return invoiceService.get(SecurityUtils.current(), id);
    }

    @PostMapping
    public DetailResponse create(@Valid @RequestBody UpsertRequest request) {
        return invoiceService.create(SecurityUtils.current(), request);
    }

    @PutMapping("/{id}")
    public DetailResponse update(@PathVariable UUID id, @Valid @RequestBody UpsertRequest request) {
        return invoiceService.update(SecurityUtils.current(), id, request);
    }

    @PatchMapping("/{id}/status")
    public DetailResponse changeStatus(@PathVariable UUID id, @RequestBody Map<String, InvoiceStatus> body) {
        return invoiceService.changeStatus(SecurityUtils.current(), id, body.get("status"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        invoiceService.delete(SecurityUtils.current(), id);
    }

    @PostMapping(path = "/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DetailResponse attach(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return invoiceService.attach(SecurityUtils.current(), id, file);
    }

    @GetMapping("/{id}/files/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID id, @PathVariable UUID fileId) {
        FileDownload download = invoiceService.download(SecurityUtils.current(), id, fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.name(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(download.contentType()))
                .body(download.bytes());
    }
}
