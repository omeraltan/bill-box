package com.billbox.alert;

import com.billbox.common.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    long countByOrganizationIdAndReadAtIsNull(UUID organizationId);

    boolean existsByOrganizationIdAndInvoiceIdAndType(UUID organizationId, UUID invoiceId, AlertType type);

    Optional<Alert> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
