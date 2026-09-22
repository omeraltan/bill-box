package com.billbox.warranty;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarrantyRepository extends JpaRepository<Warranty, UUID> {

    @EntityGraph(attributePaths = {"vendor", "receipt", "files"})
    @Query("""
            select w from Warranty w
            where w.id = :id
              and w.organization.id = :organizationId
            """)
    Optional<Warranty> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = "receipt")
    @Query(
            value = """
                    select w from Warranty w
                    where w.organization.id = :organizationId
                    """,
            countQuery = """
                    select count(w) from Warranty w
                    where w.organization.id = :organizationId
                    """
    )
    Page<Warranty> findByOrganizationId(UUID organizationId, Pageable pageable);

    @EntityGraph(attributePaths = "receipt")
    @Query("""
            select w from Warranty w
            where w.organization.id = :organizationId
            order by w.warrantyEndsOn asc
            """)
    List<Warranty> findByOrganizationIdOrderByWarrantyEndsOnAsc(UUID organizationId);

    @EntityGraph(attributePaths = "files")
    @Query("""
            select w from Warranty w
            where w.organization.id = :organizationId
              and w.warrantyEndsOn >= :date
            order by w.warrantyEndsOn asc
            fetch first 5 rows only
            """)
    List<Warranty> findTop5ByOrganizationIdAndWarrantyEndsOnGreaterThanEqualOrderByWarrantyEndsOnAsc(
            UUID organizationId,
            LocalDate date
    );

    @Query("""
            select count(w) from Warranty w
            where w.organization.id = :organizationId
              and w.warrantyEndsOn between :from and :to
            """)
    long countByOrganizationIdAndWarrantyEndsOnBetween(UUID organizationId, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = "organization")
    @Query("""
            select w from Warranty w
            where w.warrantyEndsOn <= :date
            """)
    List<Warranty> findByWarrantyEndsOnLessThanEqual(LocalDate date);
}
