package com.billbox.invoice;

import com.billbox.category.Category;
import com.billbox.common.domain.BaseEntity;
import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.InvoiceStatus;
import com.billbox.organization.Organization;
import com.billbox.recurring.RecurringRule;
import com.billbox.user.UserAccount;
import com.billbox.vendor.Vendor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_rule_id")
    private RecurringRule recurringRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private UserAccount createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceDirection direction;

    @Column(name = "invoice_number", length = 80)
    private String invoiceNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "vat_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "payment_method", length = 40)
    private String paymentMethod;

    @Column(length = 2000)
    private String notes;

    @Column(name = "ubl_xml", columnDefinition = "TEXT")
    private String ublXml;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private List<InvoiceLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceFile> files = new ArrayList<>();

    public void replaceLines(List<InvoiceLine> newLines) {
        lines.clear();
        newLines.forEach(line -> {
            line.setInvoice(this);
            lines.add(line);
        });
    }
}
