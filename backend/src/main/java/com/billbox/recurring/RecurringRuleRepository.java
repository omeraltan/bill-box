package com.billbox.recurring;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringRuleRepository extends JpaRepository<RecurringRule, UUID> {

    @EntityGraph(attributePaths = {"vendor", "category"})
    List<RecurringRule> findByOrganizationIdOrderByNextDueDateAsc(UUID organizationId);

    Optional<RecurringRule> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = {"vendor", "category", "organization"})
    List<RecurringRule> findByActiveTrueAndNextDueDateLessThanEqual(LocalDate date);
}
