package com.billbox.warranty;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarrantyRepository extends JpaRepository<Warranty, UUID> {

    @EntityGraph(attributePaths = {"vendor", "receipt", "files"})
    Optional<Warranty> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = "receipt")
    Page<Warranty> findByOrganizationId(UUID organizationId, Pageable pageable);

    @EntityGraph(attributePaths = "receipt")
    List<Warranty> findByOrganizationIdOrderByWarrantyEndsOnAsc(UUID organizationId);

    @EntityGraph(attributePaths = "files")
    List<Warranty> findTop5ByOrganizationIdAndWarrantyEndsOnGreaterThanEqualOrderByWarrantyEndsOnAsc(
            UUID organizationId,
            LocalDate date
    );

    long countByOrganizationIdAndWarrantyEndsOnBetween(UUID organizationId, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = "organization")
    List<Warranty> findByWarrantyEndsOnLessThanEqual(LocalDate date);
}
