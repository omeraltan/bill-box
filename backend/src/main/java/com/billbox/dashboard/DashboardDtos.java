package com.billbox.dashboard;

import com.billbox.alert.AlertDtos;
import com.billbox.invoice.InvoiceDtos.SummaryResponse;
import com.billbox.warranty.CoverageStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardDtos(
        BigDecimal monthExpense,
        BigDecimal monthIncome,
        BigDecimal monthNet,
        long overdueCount,
        long pendingCount,
        long unreadAlerts,
        List<NamedAmount> expenseByCategory,
        List<NamedAmount> monthlyExpense,
        List<NamedAmount> monthlyIncome,
        List<SummaryResponse> recentInvoices,
        List<AlertDtos> latestAlerts,
        BigDecimal monthReceipts,
        long expiringWarrantyCount,
        List<WarrantyBrief> expiringWarranties
) {
    public record NamedAmount(String name, BigDecimal amount) {
    }

    public record WarrantyBrief(
            UUID id,
            String productName,
            String brand,
            LocalDate warrantyEndsOn,
            CoverageStatus status,
            long daysRemaining
    ) {
    }
}
