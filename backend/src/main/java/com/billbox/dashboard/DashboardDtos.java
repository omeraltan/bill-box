package com.billbox.dashboard;

import com.billbox.alert.AlertDtos;
import com.billbox.invoice.InvoiceDtos.SummaryResponse;

import java.math.BigDecimal;
import java.util.List;

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
        List<AlertDtos> latestAlerts
) {
    public record NamedAmount(String name, BigDecimal amount) {
    }
}
