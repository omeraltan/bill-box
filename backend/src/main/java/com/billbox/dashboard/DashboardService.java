package com.billbox.dashboard;

import com.billbox.alert.AlertDtos;
import com.billbox.alert.AlertRepository;
import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.InvoiceStatus;
import com.billbox.dashboard.DashboardDtos.NamedAmount;
import com.billbox.dashboard.DashboardDtos.WarrantyBrief;
import com.billbox.invoice.InvoiceMapper;
import com.billbox.invoice.InvoiceRepository;
import com.billbox.receipt.ReceiptRepository;
import com.billbox.security.AuthPrincipal;
import com.billbox.warranty.CoverageStatus;
import com.billbox.warranty.WarrantyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final AlertRepository alertRepository;
    private final ReceiptRepository receiptRepository;
    private final WarrantyRepository warrantyRepository;

    public DashboardService(
            InvoiceRepository invoiceRepository,
            AlertRepository alertRepository,
            ReceiptRepository receiptRepository,
            WarrantyRepository warrantyRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.alertRepository = alertRepository;
        this.receiptRepository = receiptRepository;
        this.warrantyRepository = warrantyRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDtos load(AuthPrincipal principal) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate trendStart = monthStart.minusMonths(5);
        UUID org = principal.organizationId();

        BigDecimal expense = invoiceRepository.sumTotal(org, InvoiceDirection.EXPENSE, monthStart, today);
        BigDecimal income = invoiceRepository.sumTotal(org, InvoiceDirection.INCOME, monthStart, today);
        List<NamedAmount> byCategory = invoiceRepository.sumByCategory(org, InvoiceDirection.EXPENSE, monthStart, today)
                .stream()
                .map(row -> new NamedAmount((String) row[0], (BigDecimal) row[1]))
                .toList();
        List<NamedAmount> monthlyExpense = invoiceRepository.sumByMonth(org, InvoiceDirection.EXPENSE, trendStart, today)
                .stream()
                .map(row -> new NamedAmount((String) row[0], (BigDecimal) row[1]))
                .toList();
        List<NamedAmount> monthlyIncome = invoiceRepository.sumByMonth(org, InvoiceDirection.INCOME, trendStart, today)
                .stream()
                .map(row -> new NamedAmount((String) row[0], (BigDecimal) row[1]))
                .toList();

        return new DashboardDtos(
                expense,
                income,
                income.subtract(expense),
                invoiceRepository.countByStatus(org, InvoiceStatus.OVERDUE),
                invoiceRepository.countByStatus(org, InvoiceStatus.PENDING),
                alertRepository.countByOrganizationIdAndReadAtIsNull(org),
                byCategory,
                monthlyExpense,
                monthlyIncome,
                invoiceRepository.findTop8ByOrganizationIdOrderByIssueDateDesc(org).stream()
                        .map(InvoiceMapper::toSummary)
                        .toList(),
                alertRepository.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
                        .limit(5)
                        .map(AlertDtos::from)
                        .toList(),
                receiptRepository.sumTotal(org, monthStart, today),
                warrantyRepository.countByOrganizationIdAndWarrantyEndsOnBetween(org, today, today.plusDays(30)),
                warrantyRepository.findTop5ByOrganizationIdAndWarrantyEndsOnGreaterThanEqualOrderByWarrantyEndsOnAsc(org, today)
                        .stream()
                        .map(item -> new WarrantyBrief(
                                item.getId(),
                                item.getProductName(),
                                item.getBrand(),
                                item.getWarrantyEndsOn(),
                                CoverageStatus.of(item.getWarrantyEndsOn(), today),
                                CoverageStatus.daysRemaining(item.getWarrantyEndsOn(), today)
                        ))
                        .toList()
        );
    }
}
