package com.billbox.budget;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    @EntityGraph(attributePaths = "category")
    List<Budget> findByOrganizationIdAndYearOrderByMonthAsc(UUID organizationId, int year);

    Optional<Budget> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
