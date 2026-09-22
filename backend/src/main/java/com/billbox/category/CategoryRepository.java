package com.billbox.category;

import com.billbox.common.enums.CategoryKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("""
            select c from Category c
            where c.organization.id = :organizationId
            order by c.name asc
            """)
    List<Category> findByOrganizationIdOrderByNameAsc(UUID organizationId);

    @Query("""
            select c from Category c
            where c.id = :id
              and c.organization.id = :organizationId
            """)
    Optional<Category> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("""
            select count(c) > 0 from Category c
            where c.organization.id = :organizationId
              and lower(c.name) = lower(:name)
              and c.kind = :kind
            """)
    boolean existsByOrganizationIdAndNameIgnoreCaseAndKind(UUID organizationId, String name, CategoryKind kind);
}
