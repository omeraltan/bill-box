package com.billbox.alert;

import com.billbox.common.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    @Query("""
            select a from Alert a
            where a.organization.id = :organizationId
            order by a.createdAt desc
            """)
    List<Alert> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    @Query("""
            select count(a) from Alert a
            where a.organization.id = :organizationId
              and a.readAt is null
            """)
    long countByOrganizationIdAndReadAtIsNull(UUID organizationId);

    @Query("""
            select count(a) > 0 from Alert a
            where a.organization.id = :organizationId
              and a.invoice.id = :invoiceId
              and a.type = :type
            """)
    boolean existsByOrganizationIdAndInvoiceIdAndType(UUID organizationId, UUID invoiceId, AlertType type);

    @Query("""
            select count(a) > 0 from Alert a
            where a.organization.id = :organizationId
              and a.warranty.id = :warrantyId
              and a.type = :type
            """)
    boolean existsByOrganizationIdAndWarrantyIdAndType(UUID organizationId, UUID warrantyId, AlertType type);

    @Query("""
            select count(a) > 0 from Alert a
            where a.organization.id = :organizationId
              and a.receipt.id = :receiptId
              and a.type = :type
            """)
    boolean existsByOrganizationIdAndReceiptIdAndType(UUID organizationId, UUID receiptId, AlertType type);

    @Query("""
            select a from Alert a
            where a.id = :id
              and a.organization.id = :organizationId
            """)
    Optional<Alert> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
