package com.billbox.ocr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OcrResult(
        String vendorName,
        String taxNumber,
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        String iban,
        String currency,
        BigDecimal subtotal,
        BigDecimal vatAmount,
        BigDecimal total,
        String rawText,
        List<String> warnings
) {
}
