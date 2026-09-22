package com.billbox.vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    @Query(
            value = """
                    select v from Vendor v
                    where v.organization.id = :organizationId
                      and lower(v.name) like lower(concat('%', :name, '%'))
                    """,
            countQuery = """
                    select count(v) from Vendor v
                    where v.organization.id = :organizationId
                      and lower(v.name) like lower(concat('%', :name, '%'))
                    """
    )
    Page<Vendor> findByOrganizationIdAndNameContainingIgnoreCase(UUID organizationId, String name, Pageable pageable);

    @Query("""
            select v from Vendor v
            where v.organization.id = :organizationId
            order by v.name asc
            """)
    List<Vendor> findByOrganizationIdOrderByNameAsc(UUID organizationId);

    @Query("""
            select v from Vendor v
            where v.id = :id
              and v.organization.id = :organizationId
            """)
    Optional<Vendor> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("""
            select v from Vendor v
            where v.organization.id = :organizationId
              and v.taxNumber = :taxNumber
            fetch first 1 row only
            """)
    Optional<Vendor> findFirstByOrganizationIdAndTaxNumber(UUID organizationId, String taxNumber);

    @Query("""
            select v from Vendor v
            where v.organization.id = :organizationId
              and lower(v.name) = lower(:name)
            fetch first 1 row only
            """)
    Optional<Vendor> findFirstByOrganizationIdAndNameIgnoreCase(UUID organizationId, String name);
}
