package com.billbox.alert;

import com.billbox.common.enums.AlertSeverity;
import com.billbox.common.enums.AlertType;

import java.time.Instant;
import java.util.UUID;

public record AlertDtos(
        UUID id,
        AlertType type,
        AlertSeverity severity,
        String title,
        String message,
        UUID invoiceId,
        Instant createdAt,
        Instant readAt
) {
    public static AlertDtos from(Alert alert) {
        return new AlertDtos(
                alert.getId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getInvoice() == null ? null : alert.getInvoice().getId(),
                alert.getCreatedAt(),
                alert.getReadAt()
        );
    }
}
