package com.billbox.warranty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum CoverageStatus {
    ACTIVE,
    EXPIRING,
    EXPIRED;

    public static CoverageStatus of(LocalDate end, LocalDate today) {
        if (end == null || end.isAfter(today.plusDays(30))) {
            return ACTIVE;
        }
        if (end.isBefore(today)) {
            return EXPIRED;
        }
        return EXPIRING;
    }

    public static long daysRemaining(LocalDate end, LocalDate today) {
        if (end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(today, end);
    }
}
