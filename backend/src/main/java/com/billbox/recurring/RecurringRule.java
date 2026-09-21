package com.billbox.recurring;

import com.billbox.category.Category;
import com.billbox.common.domain.BaseEntity;
import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.RecurringInterval;
import com.billbox.organization.Organization;
import com.billbox.vendor.Vendor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "recurring_rules")
public class RecurringRule extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceDirection direction;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    @Enumerated(EnumType.STRING)
    @Column(name = "interval", nullable = false, length = 20)
    private RecurringInterval interval;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_generated_at")
    private Instant lastGeneratedAt;

    @Column(length = 1000)
    private String notes;
}
