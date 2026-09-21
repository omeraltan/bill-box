package com.billbox.budget;

import com.billbox.budget.BudgetDtos.Response;
import com.billbox.budget.BudgetDtos.UpsertRequest;
import com.billbox.category.CategoryService;
import com.billbox.common.exception.ApiException;
import com.billbox.organization.OrganizationRepository;
import com.billbox.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final OrganizationRepository organizationRepository;
    private final CategoryService categoryService;

    public BudgetService(
            BudgetRepository budgetRepository,
            OrganizationRepository organizationRepository,
            CategoryService categoryService
    ) {
        this.budgetRepository = budgetRepository;
        this.organizationRepository = organizationRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public List<Response> list(AuthPrincipal principal, Integer year) {
        int effectiveYear = year == null ? LocalDate.now().getYear() : year;
        return budgetRepository.findByOrganizationIdAndYearOrderByMonthAsc(principal.organizationId(), effectiveYear)
                .stream()
                .map(Response::from)
                .toList();
    }

    @Transactional
    public Response create(AuthPrincipal principal, UpsertRequest request) {
        Budget budget = new Budget();
        budget.setOrganization(organizationRepository.getReferenceById(principal.organizationId()));
        apply(principal, budget, request);
        budgetRepository.save(budget);
        return Response.from(budget);
    }

    @Transactional
    public Response update(AuthPrincipal principal, UUID id, UpsertRequest request) {
        Budget budget = budgetRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Bütçe bulunamadı."));
        apply(principal, budget, request);
        return Response.from(budget);
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID id) {
        Budget budget = budgetRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Bütçe bulunamadı."));
        budgetRepository.delete(budget);
    }

    private void apply(AuthPrincipal principal, Budget budget, UpsertRequest request) {
        budget.setYear(request.year());
        budget.setMonth(request.month());
        budget.setAmount(request.amount());
        budget.setCurrency(request.currency() == null || request.currency().isBlank() ? "TRY" : request.currency());
        budget.setCategory(request.categoryId() == null ? null : categoryService.require(principal, request.categoryId()));
    }
}
