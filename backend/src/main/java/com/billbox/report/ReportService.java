package com.billbox.report;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.invoice.InvoiceRepository;
import com.billbox.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final InvoiceRepository invoiceRepository;

    public ReportService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public ReportSummary summary(AuthPrincipal principal, LocalDate from, LocalDate to, InvoiceDirection direction) {
        UUID org = principal.organizationId();
        InvoiceDirection effective = direction == null ? InvoiceDirection.EXPENSE : direction;
        return new ReportSummary(
                invoiceRepository.sumTotal(org, InvoiceDirection.EXPENSE, from, to),
                invoiceRepository.sumTotal(org, InvoiceDirection.INCOME, from, to),
                toNamed(invoiceRepository.sumByCategory(org, effective, from, to)),
                toNamed(invoiceRepository.sumByVendor(org, effective, from, to)),
                toNamed(invoiceRepository.sumByMonth(org, effective, from, to))
        );
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(AuthPrincipal principal, LocalDate from, LocalDate to, InvoiceDirection direction) {
        ReportSummary data = summary(principal, from, to, direction);
        StringBuilder builder = new StringBuilder("Tür,Ad,Tutar\n");
        data.byCategory().forEach(item -> builder.append("Kategori,")
                .append(escape(item.name())).append(',').append(item.amount()).append('\n'));
        data.byVendor().forEach(item -> builder.append("Cari,")
                .append(escape(item.name())).append(',').append(item.amount()).append('\n'));
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<NamedAmount> toNamed(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new NamedAmount((String) row[0], (BigDecimal) row[1]))
                .toList();
    }

    private String escape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public record ReportSummary(
            BigDecimal totalExpense,
            BigDecimal totalIncome,
            List<NamedAmount> byCategory,
            List<NamedAmount> byVendor,
            List<NamedAmount> byMonth
    ) {
    }

    public record NamedAmount(String name, BigDecimal amount) {
    }
}
