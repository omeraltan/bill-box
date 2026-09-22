package com.billbox.recurring;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringRuleRepository extends JpaRepository<RecurringRule, UUID> {

    @EntityGraph(attributePaths = {"vendor", "category"})
    @Query("""
            select r from RecurringRule r
            where r.organization.id = :organizationId
            order by r.nextDueDate asc
            """)
    List<RecurringRule> findByOrganizationIdOrderByNextDueDateAsc(UUID organizationId);

    @Query("""
            select r from RecurringRule r
            where r.id = :id
              and r.organization.id = :organizationId
            """)
    Optional<RecurringRule> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = {"vendor", "category", "organization"})
    @Query("""
            select r from RecurringRule r
            where r.active = true
              and r.nextDueDate <= :date
            """)
    List<RecurringRule> findByActiveTrueAndNextDueDateLessThanEqual(LocalDate date);
}
