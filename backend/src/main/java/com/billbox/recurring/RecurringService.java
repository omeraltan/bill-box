package com.billbox.recurring;

import com.billbox.category.CategoryService;
import com.billbox.common.enums.InvoiceStatus;
import com.billbox.common.exception.ApiException;
import com.billbox.invoice.InvoiceDtos;
import com.billbox.invoice.InvoiceService;
import com.billbox.organization.Membership;
import com.billbox.organization.MembershipRepository;
import com.billbox.organization.OrganizationRepository;
import com.billbox.recurring.RecurringDtos.Response;
import com.billbox.security.AuthPrincipal;
import com.billbox.vendor.VendorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RecurringService {

    private final RecurringRuleRepository recurringRuleRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final VendorService vendorService;
    private final CategoryService categoryService;
    private final InvoiceService invoiceService;

    public RecurringService(
            RecurringRuleRepository recurringRuleRepository,
            OrganizationRepository organizationRepository,
            MembershipRepository membershipRepository,
            VendorService vendorService,
            CategoryService categoryService,
            InvoiceService invoiceService
    ) {
        this.recurringRuleRepository = recurringRuleRepository;
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.vendorService = vendorService;
        this.categoryService = categoryService;
        this.invoiceService = invoiceService;
    }

    @Transactional(readOnly = true)
    public List<Response> list(AuthPrincipal principal) {
        return recurringRuleRepository.findByOrganizationIdOrderByNextDueDateAsc(principal.organizationId())
                .stream()
                .map(Response::from)
                .toList();
    }

    @Transactional
    public Response create(AuthPrincipal principal, RecurringDtos.UpsertRequest request) {
        RecurringRule rule = new RecurringRule();
        rule.setOrganization(organizationRepository.getReferenceById(principal.organizationId()));
        apply(principal, rule, request);
        recurringRuleRepository.save(rule);
        return Response.from(rule);
    }

    @Transactional
    public Response update(AuthPrincipal principal, UUID id, RecurringDtos.UpsertRequest request) {
        RecurringRule rule = require(principal, id);
        apply(principal, rule, request);
        return Response.from(rule);
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID id) {
        recurringRuleRepository.delete(require(principal, id));
    }

    @Transactional
    public void generateDueRules() {
        LocalDate today = LocalDate.now();
        for (RecurringRule rule : recurringRuleRepository.findByActiveTrueAndNextDueDateLessThanEqual(today)) {
            Membership membership = membershipRepository.findByOrganizationId(rule.getOrganization().getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (membership == null) {
                continue;
            }
            AuthPrincipal principal = new AuthPrincipal(
                    membership.getUser().getId(),
                    membership.getOrganization().getId(),
                    membership.getUser().getEmail(),
                    membership.getUser().getFullName(),
                    membership.getOrganization().getName(),
                    membership.getRole()
            );
            invoiceService.create(principal, new InvoiceDtos.UpsertRequest(
                    rule.getDirection(),
                    rule.getVendor() == null ? null : rule.getVendor().getId(),
                    rule.getCategory() == null ? null : rule.getCategory().getId(),
                    "TEK-" + rule.getId().toString().substring(0, 8),
                    rule.getNextDueDate(),
                    rule.getNextDueDate(),
                    null,
                    rule.getCurrency(),
                    InvoiceStatus.PENDING,
                    null,
                    rule.getTitle() + " tekrarlayan fatura",
                    rule.getAmount(),
                    BigDecimal.ZERO,
                    rule.getAmount(),
                    List.of()
            ));
            rule.setLastGeneratedAt(Instant.now());
            rule.setNextDueDate(nextDate(rule));
        }
    }

    private RecurringRule require(AuthPrincipal principal, UUID id) {
        return recurringRuleRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Tekrarlayan fatura bulunamadı."));
    }

    private void apply(AuthPrincipal principal, RecurringRule rule, RecurringDtos.UpsertRequest request) {
        rule.setTitle(request.title().trim());
        rule.setDirection(request.direction());
        rule.setAmount(request.amount());
        rule.setCurrency(request.currency() == null || request.currency().isBlank() ? "TRY" : request.currency());
        rule.setInterval(request.interval());
        rule.setNextDueDate(request.nextDueDate());
        rule.setDayOfMonth(request.dayOfMonth());
        rule.setActive(request.active() == null || request.active());
        rule.setNotes(request.notes());
        rule.setVendor(request.vendorId() == null ? null : vendorService.require(principal, request.vendorId()));
        rule.setCategory(request.categoryId() == null ? null : categoryService.require(principal, request.categoryId()));
    }

    private LocalDate nextDate(RecurringRule rule) {
        return switch (rule.getInterval()) {
            case WEEKLY -> rule.getNextDueDate().plusWeeks(1);
            case MONTHLY -> rule.getNextDueDate().plusMonths(1);
            case QUARTERLY -> rule.getNextDueDate().plusMonths(3);
            case YEARLY -> rule.getNextDueDate().plusYears(1);
        };
    }
}
