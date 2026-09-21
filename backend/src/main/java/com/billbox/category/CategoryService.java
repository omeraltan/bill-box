package com.billbox.category;

import com.billbox.category.CategoryDtos.Response;
import com.billbox.category.CategoryDtos.UpsertRequest;
import com.billbox.common.exception.ApiException;
import com.billbox.organization.OrganizationRepository;
import com.billbox.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;

    public CategoryService(CategoryRepository categoryRepository, OrganizationRepository organizationRepository) {
        this.categoryRepository = categoryRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public List<Response> list(AuthPrincipal principal) {
        return categoryRepository.findByOrganizationIdOrderByNameAsc(principal.organizationId())
                .stream()
                .map(Response::from)
                .toList();
    }

    @Transactional
    public Response create(AuthPrincipal principal, UpsertRequest request) {
        if (categoryRepository.existsByOrganizationIdAndNameIgnoreCaseAndKind(
                principal.organizationId(), request.name().trim(), request.kind())) {
            throw ApiException.conflict("Bu kategori zaten mevcut.");
        }
        Category category = new Category();
        category.setOrganization(organizationRepository.getReferenceById(principal.organizationId()));
        apply(category, request);
        categoryRepository.save(category);
        return Response.from(category);
    }

    @Transactional
    public Response update(AuthPrincipal principal, UUID id, UpsertRequest request) {
        Category category = require(principal, id);
        apply(category, request);
        return Response.from(category);
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID id) {
        categoryRepository.delete(require(principal, id));
    }

    public Category require(AuthPrincipal principal, UUID id) {
        return categoryRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Kategori bulunamadı."));
    }

    private void apply(Category category, UpsertRequest request) {
        category.setName(request.name().trim());
        category.setKind(request.kind());
        category.setColor(request.color());
        category.setIcon(request.icon());
    }
}
