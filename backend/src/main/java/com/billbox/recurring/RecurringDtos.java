package com.billbox.recurring;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.RecurringInterval;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class RecurringDtos {

    private RecurringDtos() {
    }

    public record UpsertRequest(
            UUID vendorId,
            UUID categoryId,
            @NotNull InvoiceDirection direction,
            @NotBlank @Size(max = 200) String title,
            @NotNull BigDecimal amount,
            String currency,
            @NotNull RecurringInterval interval,
            @NotNull LocalDate nextDueDate,
            Integer dayOfMonth,
            Boolean active,
            @Size(max = 1000) String notes
    ) {
    }

    public record Response(
            UUID id,
            UUID vendorId,
            String vendorName,
            UUID categoryId,
            String categoryName,
            InvoiceDirection direction,
            String title,
            BigDecimal amount,
            String currency,
            RecurringInterval interval,
            LocalDate nextDueDate,
            Integer dayOfMonth,
            boolean active,
            String notes
    ) {
        public static Response from(RecurringRule rule) {
            return new Response(
                    rule.getId(),
                    rule.getVendor() == null ? null : rule.getVendor().getId(),
                    rule.getVendor() == null ? null : rule.getVendor().getName(),
                    rule.getCategory() == null ? null : rule.getCategory().getId(),
                    rule.getCategory() == null ? null : rule.getCategory().getName(),
                    rule.getDirection(),
                    rule.getTitle(),
                    rule.getAmount(),
                    rule.getCurrency(),
                    rule.getInterval(),
                    rule.getNextDueDate(),
                    rule.getDayOfMonth(),
                    rule.isActive(),
                    rule.getNotes()
            );
        }
    }
}
