package com.billbox.warranty;

import com.billbox.common.domain.BaseEntity;
import com.billbox.organization.Organization;
import com.billbox.receipt.Receipt;
import com.billbox.user.UserAccount;
import com.billbox.vendor.Vendor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "warranties")
public class Warranty extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private Receipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private UserAccount createdBy;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(length = 120)
    private String brand;

    @Column(name = "serial_number", length = 120)
    private String serialNumber;

    @Column(name = "merchant_name", length = 200)
    private String merchantName;

    @Column(name = "purchased_on", nullable = false)
    private LocalDate purchasedOn;

    @Column(name = "warranty_ends_on", nullable = false)
    private LocalDate warrantyEndsOn;

    @Column(name = "return_until")
    private LocalDate returnUntil;

    @Column(length = 2000)
    private String notes;

    @OneToMany(mappedBy = "warranty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WarrantyFile> files = new ArrayList<>();
}
