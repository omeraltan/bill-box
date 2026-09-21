package com.billbox.warranty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class WarrantyDtos {

    private WarrantyDtos() {
    }

    public record UpsertRequest(
            UUID receiptId,
            UUID vendorId,
            @NotBlank @Size(max = 200) String productName,
            @Size(max = 120) String brand,
            @Size(max = 120) String serialNumber,
            @Size(max = 200) String merchantName,
            @NotNull LocalDate purchasedOn,
            @NotNull LocalDate warrantyEndsOn,
            LocalDate returnUntil,
            @Size(max = 2000) String notes
    ) {
    }

    public record FileResponse(UUID id, String originalName, String contentType, long sizeBytes, Instant createdAt) {
    }

    public record SummaryResponse(
            UUID id,
            String productName,
            String brand,
            String merchantName,
            LocalDate purchasedOn,
            LocalDate warrantyEndsOn,
            LocalDate returnUntil,
            CoverageStatus status,
            long daysRemaining,
            UUID receiptId
    ) {
    }

    public record DetailResponse(
            UUID id,
            UUID receiptId,
            UUID vendorId,
            String productName,
            String brand,
            String serialNumber,
            String merchantName,
            LocalDate purchasedOn,
            LocalDate warrantyEndsOn,
            LocalDate returnUntil,
            String notes,
            CoverageStatus status,
            long daysRemaining,
            List<FileResponse> files
    ) {
    }
}
