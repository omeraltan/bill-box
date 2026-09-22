package com.billbox.invoice;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    @EntityGraph(attributePaths = {"vendor", "category"})
    @Query("""
            select i from Invoice i
            where i.id = :id
              and i.organization.id = :organizationId
            """)
    Optional<Invoice> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("""
            select i from Invoice i
            where i.organization.id = :organizationId
              and i.status in :statuses
              and i.dueDate <= :date
            """)
    List<Invoice> findByOrganizationIdAndStatusInAndDueDateLessThanEqual(
            UUID organizationId,
            List<InvoiceStatus> statuses,
            LocalDate date
    );

    @Query("""
            select i from Invoice i
            where i.status in :statuses
              and i.dueDate <= :date
            """)
    List<Invoice> findByStatusInAndDueDateLessThanEqual(List<InvoiceStatus> statuses, LocalDate date);

    @Query("""
            select coalesce(sum(i.total), 0)
            from Invoice i
            where i.organization.id = :organizationId
              and i.direction = :direction
              and i.status <> com.billbox.common.enums.InvoiceStatus.CANCELLED
              and i.issueDate between :from and :to
            """)
    BigDecimal sumTotal(UUID organizationId, InvoiceDirection direction, LocalDate from, LocalDate to);

    @Query("""
            select count(i)
            from Invoice i
            where i.organization.id = :organizationId
              and i.status = :status
            """)
    long countByStatus(UUID organizationId, InvoiceStatus status);

    @Query("""
            select i.category.name, coalesce(sum(i.total), 0)
            from Invoice i
            where i.organization.id = :organizationId
              and i.direction = :direction
              and i.status <> com.billbox.common.enums.InvoiceStatus.CANCELLED
              and i.issueDate between :from and :to
              and i.category is not null
            group by i.category.name
            order by sum(i.total) desc
            """)
    List<Object[]> sumByCategory(UUID organizationId, InvoiceDirection direction, LocalDate from, LocalDate to);

    @Query("""
            select i.vendor.name, coalesce(sum(i.total), 0)
            from Invoice i
            where i.organization.id = :organizationId
              and i.direction = :direction
              and i.status <> com.billbox.common.enums.InvoiceStatus.CANCELLED
              and i.issueDate between :from and :to
              and i.vendor is not null
            group by i.vendor.name
            order by sum(i.total) desc
            """)
    List<Object[]> sumByVendor(UUID organizationId, InvoiceDirection direction, LocalDate from, LocalDate to);

    @Query("""
            select function('to_char', i.issueDate, 'YYYY-MM'), coalesce(sum(i.total), 0)
            from Invoice i
            where i.organization.id = :organizationId
              and i.direction = :direction
              and i.status <> com.billbox.common.enums.InvoiceStatus.CANCELLED
              and i.issueDate between :from and :to
            group by function('to_char', i.issueDate, 'YYYY-MM')
            order by function('to_char', i.issueDate, 'YYYY-MM')
            """)
    List<Object[]> sumByMonth(UUID organizationId, InvoiceDirection direction, LocalDate from, LocalDate to);

    @Query("""
            select i from Invoice i
            where i.organization.id = :organizationId
            order by i.issueDate desc
            fetch first 8 rows only
            """)
    List<Invoice> findTop8ByOrganizationIdOrderByIssueDateDesc(UUID organizationId);

    @Query("""
            select i from Invoice i
            where i.organization.id = :organizationId
              and i.vendor.id = :vendorId
              and i.direction = :direction
              and i.status <> :status
            """)
    List<Invoice> findByOrganizationIdAndVendorIdAndDirectionAndStatusNot(
            UUID organizationId,
            UUID vendorId,
            InvoiceDirection direction,
            InvoiceStatus status
    );
}
