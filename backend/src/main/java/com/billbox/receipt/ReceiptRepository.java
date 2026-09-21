package com.billbox.receipt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {

    @EntityGraph(attributePaths = {"vendor", "category"})
    Optional<Receipt> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = "category")
    Page<Receipt> findByOrganizationIdAndMerchantNameContainingIgnoreCase(
            UUID organizationId,
            String merchantName,
            Pageable pageable
    );

    @Query("""
            select coalesce(sum(r.total), 0)
            from Receipt r
            where r.organization.id = :organizationId
              and r.purchasedOn between :from and :to
            """)
    BigDecimal sumTotal(UUID organizationId, LocalDate from, LocalDate to);

    List<Receipt> findByOrganizationIdAndReturnUntilBetween(UUID organizationId, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = "organization")
    List<Receipt> findByReturnUntilBetween(LocalDate from, LocalDate to);
}
