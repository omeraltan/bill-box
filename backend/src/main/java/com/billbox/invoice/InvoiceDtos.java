package com.billbox.invoice;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.InvoiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class InvoiceDtos {

    private InvoiceDtos() {
    }

    public record LineRequest(
            @Size(max = 300) String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal vatRate
    ) {
    }

    public record UpsertRequest(
            @NotNull InvoiceDirection direction,
            UUID vendorId,
            UUID categoryId,
            @Size(max = 80) String invoiceNumber,
            @NotNull LocalDate issueDate,
            LocalDate dueDate,
            LocalDate paidDate,
            @Size(max = 3) String currency,
            InvoiceStatus status,
            @Size(max = 40) String paymentMethod,
            @Size(max = 2000) String notes,
            BigDecimal subtotal,
            BigDecimal vatAmount,
            BigDecimal total,
            @Valid List<LineRequest> lines
    ) {
    }

    public record LineResponse(
            UUID id,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal vatRate,
            BigDecimal lineTotal,
            int sortOrder
    ) {
    }

    public record FileResponse(
            UUID id,
            String originalName,
            String contentType,
            long sizeBytes,
            Instant createdAt
    ) {
    }

    public record SummaryResponse(
            UUID id,
            InvoiceDirection direction,
            InvoiceStatus status,
            String invoiceNumber,
            LocalDate issueDate,
            LocalDate dueDate,
            LocalDate paidDate,
            String currency,
            BigDecimal total,
            String vendorName,
            String categoryName,
            String categoryColor
    ) {
    }

    public record DetailResponse(
            UUID id,
            InvoiceDirection direction,
            InvoiceStatus status,
            String invoiceNumber,
            LocalDate issueDate,
            LocalDate dueDate,
            LocalDate paidDate,
            String currency,
            BigDecimal subtotal,
            BigDecimal vatAmount,
            BigDecimal total,
            String paymentMethod,
            String notes,
            UUID vendorId,
            String vendorName,
            UUID categoryId,
            String categoryName,
            List<LineResponse> lines,
            List<FileResponse> files
    ) {
    }
}
