package com.billbox.vendor;

import com.billbox.common.domain.BaseEntity;
import com.billbox.common.enums.PartyType;
import com.billbox.organization.Organization;
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

@Getter
@Setter
@Entity
@Table(name = "vendors")
public class Vendor extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "tax_number", length = 20)
    private String taxNumber;

    @Column(length = 180)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 34)
    private String iban;

    @Column(length = 400)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false, length = 20)
    private PartyType partyType;

    @Column(length = 1000)
    private String notes;
}
