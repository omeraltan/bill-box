package com.billbox.report;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.report.ReportService.ReportSummary;
import com.billbox.security.SecurityUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ReportSummary summary(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) InvoiceDirection direction
    ) {
        return reportService.summary(SecurityUtils.current(), from, to, direction);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) InvoiceDirection direction
    ) {
        byte[] csv = reportService.exportCsv(SecurityUtils.current(), from, to, direction);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("bill-box-rapor.csv", StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
