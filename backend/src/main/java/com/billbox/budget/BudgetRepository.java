package com.billbox.budget;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    @EntityGraph(attributePaths = "category")
    @Query("""
            select b from Budget b
            where b.organization.id = :organizationId
              and b.year = :year
            order by b.month asc
            """)
    List<Budget> findByOrganizationIdAndYearOrderByMonthAsc(UUID organizationId, int year);

    @Query("""
            select b from Budget b
            where b.id = :id
              and b.organization.id = :organizationId
            """)
    Optional<Budget> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
