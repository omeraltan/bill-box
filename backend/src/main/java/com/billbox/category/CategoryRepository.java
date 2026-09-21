package com.billbox.category;

import com.billbox.common.enums.CategoryKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByOrganizationIdOrderByNameAsc(UUID organizationId);

    Optional<Category> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByOrganizationIdAndNameIgnoreCaseAndKind(UUID organizationId, String name, CategoryKind kind);
}
