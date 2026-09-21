package com.billbox.receipt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ReceiptDtos {

    private ReceiptDtos() {
    }

    public record ItemRequest(
            @NotBlank @Size(max = 300) String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            Integer warrantyMonths
    ) {
    }

    public record UpsertRequest(
            UUID vendorId,
            UUID categoryId,
            @NotBlank @Size(max = 200) String merchantName,
            @NotNull LocalDate purchasedOn,
            @Size(max = 3) String currency,
            BigDecimal total,
            @Size(max = 40) String paymentMethod,
            LocalDate returnUntil,
            @Size(max = 2000) String notes,
            @Valid List<ItemRequest> items
    ) {
    }

    public record ItemResponse(
            UUID id,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            Integer warrantyMonths
    ) {
    }

    public record FileResponse(UUID id, String originalName, String contentType, long sizeBytes, Instant createdAt) {
    }

    public record SummaryResponse(
            UUID id,
            String merchantName,
            LocalDate purchasedOn,
            LocalDate returnUntil,
            String currency,
            BigDecimal total,
            String categoryName,
            String categoryColor,
            int itemCount,
            int fileCount
    ) {
    }

    public record DetailResponse(
            UUID id,
            UUID vendorId,
            String vendorName,
            UUID categoryId,
            String categoryName,
            String merchantName,
            LocalDate purchasedOn,
            String currency,
            BigDecimal total,
            String paymentMethod,
            LocalDate returnUntil,
            String notes,
            List<ItemResponse> items,
            List<FileResponse> files
    ) {
    }
}
