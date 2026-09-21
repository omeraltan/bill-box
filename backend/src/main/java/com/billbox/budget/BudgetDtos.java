package com.billbox.budget;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public final class BudgetDtos {

    private BudgetDtos() {
    }

    public record UpsertRequest(
            UUID categoryId,
            @NotNull @Min(2000) @Max(2100) Integer year,
            @Min(1) @Max(12) Integer month,
            @NotNull BigDecimal amount,
            String currency
    ) {
    }

    public record Response(
            UUID id,
            UUID categoryId,
            String categoryName,
            int year,
            Integer month,
            BigDecimal amount,
            String currency
    ) {
        public static Response from(Budget budget) {
            return new Response(
                    budget.getId(),
                    budget.getCategory() == null ? null : budget.getCategory().getId(),
                    budget.getCategory() == null ? "Genel" : budget.getCategory().getName(),
                    budget.getYear(),
                    budget.getMonth(),
                    budget.getAmount(),
                    budget.getCurrency()
            );
        }
    }
}
