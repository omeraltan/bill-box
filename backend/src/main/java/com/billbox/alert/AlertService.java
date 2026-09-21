package com.billbox.alert;

import com.billbox.common.enums.AlertSeverity;
import com.billbox.common.enums.AlertType;
import com.billbox.common.enums.InvoiceStatus;
import com.billbox.common.exception.ApiException;
import com.billbox.invoice.Invoice;
import com.billbox.invoice.InvoiceRepository;
import com.billbox.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("10000");

    private final AlertRepository alertRepository;
    private final InvoiceRepository invoiceRepository;

    public AlertService(AlertRepository alertRepository, InvoiceRepository invoiceRepository) {
        this.alertRepository = alertRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertDtos> list(AuthPrincipal principal) {
        return alertRepository.findByOrganizationIdOrderByCreatedAtDesc(principal.organizationId())
                .stream()
                .map(AlertDtos::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(AuthPrincipal principal) {
        return alertRepository.countByOrganizationIdAndReadAtIsNull(principal.organizationId());
    }

    @Transactional
    public void markRead(AuthPrincipal principal, UUID id) {
        Alert alert = alertRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Uyarı bulunamadı."));
        alert.setReadAt(Instant.now());
    }

    @Transactional
    public void markAllRead(AuthPrincipal principal) {
        alertRepository.findByOrganizationIdOrderByCreatedAtDesc(principal.organizationId()).stream()
                .filter(alert -> alert.getReadAt() == null)
                .forEach(alert -> alert.setReadAt(Instant.now()));
    }

    @Transactional
    public void evaluateInvoice(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.CANCELLED || invoice.getStatus() == InvoiceStatus.PAID) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (invoice.getDueDate() != null && !invoice.getDueDate().isAfter(today)) {
            createOnce(
                    invoice,
                    AlertType.OVERDUE,
                    AlertSeverity.CRITICAL,
                    "Vadesi geçen fatura",
                    "Fatura vadesi " + invoice.getDueDate().format(DATE) + " tarihinde doldu."
            );
            if (invoice.getStatus() == InvoiceStatus.PENDING) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
            }
        } else if (invoice.getDueDate() != null && !invoice.getDueDate().isAfter(today.plusDays(3))) {
            createOnce(
                    invoice,
                    AlertType.DUE_SOON,
                    AlertSeverity.WARNING,
                    "Yaklaşan vade",
                    "Fatura vadesi " + invoice.getDueDate().format(DATE) + " tarihinde."
            );
        }
        if (invoice.getTotal() != null && invoice.getTotal().compareTo(HIGH_AMOUNT) >= 0) {
            createOnce(
                    invoice,
                    AlertType.HIGH_AMOUNT,
                    AlertSeverity.INFO,
                    "Yüksek tutarlı fatura",
                    "Fatura tutarı " + invoice.getTotal() + " " + invoice.getCurrency() + "."
            );
        }
    }

    @Transactional
    public void scanDueInvoices() {
        LocalDate today = LocalDate.now();
        List<Invoice> invoices = invoiceRepository.findByStatusInAndDueDateLessThanEqual(
                List.of(InvoiceStatus.PENDING, InvoiceStatus.OVERDUE, InvoiceStatus.DRAFT),
                today.plusDays(3)
        );
        invoices.forEach(this::evaluateInvoice);
    }

    private void createOnce(Invoice invoice, AlertType type, AlertSeverity severity, String title, String message) {
        UUID orgId = invoice.getOrganization().getId();
        if (alertRepository.existsByOrganizationIdAndInvoiceIdAndType(orgId, invoice.getId(), type)) {
            return;
        }
        Alert alert = new Alert();
        alert.setOrganization(invoice.getOrganization());
        alert.setInvoice(invoice);
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        alertRepository.save(alert);
    }
}
