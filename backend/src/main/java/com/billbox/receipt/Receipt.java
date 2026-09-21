package com.billbox.receipt;

import com.billbox.category.Category;
import com.billbox.common.domain.BaseEntity;
import com.billbox.organization.Organization;
import com.billbox.user.UserAccount;
import com.billbox.vendor.Vendor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "receipts")
public class Receipt extends BaseEntity {

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
    @JoinColumn(name = "created_by_id")
    private UserAccount createdBy;

    @Column(name = "merchant_name", nullable = false, length = 200)
    private String merchantName;

    @Column(name = "purchased_on", nullable = false)
    private LocalDate purchasedOn;

    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 40)
    private String paymentMethod;

    @Column(name = "return_until")
    private LocalDate returnUntil;

    @Column(length = 2000)
    private String notes;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private List<ReceiptItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptFile> files = new ArrayList<>();

    public void replaceItems(List<ReceiptItem> next) {
        items.clear();
        next.forEach(item -> {
            item.setReceipt(this);
            items.add(item);
        });
    }
}
