package com.billbox.vault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VaultDtos(
        String kind,
        UUID id,
        String title,
        String subtitle,
        LocalDate occurredOn,
        LocalDate highlightOn,
        BigDecimal amount,
        String currency,
        String status,
        String categoryName
) {
}
