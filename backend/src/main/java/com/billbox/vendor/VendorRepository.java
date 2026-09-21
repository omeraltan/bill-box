package com.billbox.vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    Page<Vendor> findByOrganizationIdAndNameContainingIgnoreCase(UUID organizationId, String name, Pageable pageable);

    List<Vendor> findByOrganizationIdOrderByNameAsc(UUID organizationId);

    Optional<Vendor> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<Vendor> findFirstByOrganizationIdAndTaxNumber(UUID organizationId, String taxNumber);

    Optional<Vendor> findFirstByOrganizationIdAndNameIgnoreCase(UUID organizationId, String name);
}
