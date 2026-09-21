package com.billbox.invoice;

import com.billbox.alert.AlertService;
import com.billbox.audit.AuditService;
import com.billbox.category.CategoryService;
import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.InvoiceStatus;
import com.billbox.common.exception.ApiException;
import com.billbox.common.web.PageResponse;
import com.billbox.file.FileStorageService;
import com.billbox.invoice.InvoiceDtos.DetailResponse;
import com.billbox.invoice.InvoiceDtos.LineRequest;
import com.billbox.invoice.InvoiceDtos.SummaryResponse;
import com.billbox.invoice.InvoiceDtos.UpsertRequest;
import com.billbox.organization.OrganizationRepository;
import com.billbox.security.AuthPrincipal;
import com.billbox.user.UserAccountRepository;
import com.billbox.vendor.VendorService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userAccountRepository;
    private final VendorService vendorService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final AlertService alertService;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            OrganizationRepository organizationRepository,
            UserAccountRepository userAccountRepository,
            VendorService vendorService,
            CategoryService categoryService,
            FileStorageService fileStorageService,
            AuditService auditService,
            AlertService alertService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.organizationRepository = organizationRepository;
        this.userAccountRepository = userAccountRepository;
        this.vendorService = vendorService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SummaryResponse> search(
            AuthPrincipal principal,
            String query,
            InvoiceDirection direction,
            InvoiceStatus status,
            UUID vendorId,
            UUID categoryId,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        Specification<Invoice> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), principal.organizationId()));
            if (query != null && !query.isBlank()) {
                String like = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("invoiceNumber")), like),
                        cb.like(cb.lower(root.get("notes")), like),
                        cb.like(cb.lower(root.get("vendor").get("name")), like)
                ));
            }
            if (direction != null) {
                predicates.add(cb.equal(root.get("direction"), direction));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (vendorId != null) {
                predicates.add(cb.equal(root.get("vendor").get("id"), vendorId));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(
                invoiceRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate")))
                        .map(InvoiceMapper::toSummary)
        );
    }

    @Transactional(readOnly = true)
    public DetailResponse get(AuthPrincipal principal, UUID id) {
        return InvoiceMapper.toDetail(require(principal, id));
    }

    @Transactional
    public DetailResponse create(AuthPrincipal principal, UpsertRequest request) {
        Invoice invoice = new Invoice();
        invoice.setOrganization(organizationRepository.getReferenceById(principal.organizationId()));
        invoice.setCreatedBy(userAccountRepository.getReferenceById(principal.userId()));
        apply(principal, invoice, request);
        invoiceRepository.save(invoice);
        alertService.evaluateInvoice(invoice);
        auditService.record(principal, "CREATE", "INVOICE", invoice.getId(), label(invoice));
        return InvoiceMapper.toDetail(invoice);
    }

    @Transactional
    public DetailResponse update(AuthPrincipal principal, UUID id, UpsertRequest request) {
        Invoice invoice = require(principal, id);
        apply(principal, invoice, request);
        alertService.evaluateInvoice(invoice);
        auditService.record(principal, "UPDATE", "INVOICE", invoice.getId(), label(invoice));
        return InvoiceMapper.toDetail(invoice);
    }

    @Transactional
    public DetailResponse changeStatus(AuthPrincipal principal, UUID id, InvoiceStatus status) {
        Invoice invoice = require(principal, id);
        invoice.setStatus(status);
        if (status == InvoiceStatus.PAID && invoice.getPaidDate() == null) {
            invoice.setPaidDate(LocalDate.now());
        }
        if (status != InvoiceStatus.PAID) {
            invoice.setPaidDate(null);
        }
        auditService.record(principal, "STATUS", "INVOICE", id, status.name());
        return InvoiceMapper.toDetail(invoice);
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID id) {
        Invoice invoice = require(principal, id);
        invoice.getFiles().forEach(file -> {
            try {
                fileStorageService.delete(file.getStorageKey());
            } catch (IOException ignored) {
                // silme hatası faturayı kilitlemesin
            }
        });
        invoiceRepository.delete(invoice);
        auditService.record(principal, "DELETE", "INVOICE", id, label(invoice));
    }

    @Transactional
    public DetailResponse attach(AuthPrincipal principal, UUID id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Dosya seçilmedi.");
        }
        Invoice invoice = require(principal, id);
        try {
            String key = fileStorageService.store(principal.organizationId(), invoice.getId(), file);
            InvoiceFile invoiceFile = new InvoiceFile();
            invoiceFile.setInvoice(invoice);
            invoiceFile.setOriginalName(file.getOriginalFilename());
            invoiceFile.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            invoiceFile.setStorageKey(key);
            invoiceFile.setSizeBytes(file.getSize());
            invoice.getFiles().add(invoiceFile);
        } catch (IOException ex) {
            throw ApiException.badRequest("Dosya kaydedilemedi.");
        }
        return InvoiceMapper.toDetail(invoice);
    }

    @Transactional(readOnly = true)
    public FileDownload download(AuthPrincipal principal, UUID invoiceId, UUID fileId) {
        Invoice invoice = require(principal, invoiceId);
        InvoiceFile file = invoice.getFiles().stream()
                .filter(item -> item.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Dosya bulunamadı."));
        try {
            byte[] bytes = Files.readAllBytes(fileStorageService.resolve(file.getStorageKey()));
            return new FileDownload(file.getOriginalName(), file.getContentType(), bytes);
        } catch (IOException ex) {
            throw ApiException.notFound("Dosya okunamadı.");
        }
    }

    public Invoice require(AuthPrincipal principal, UUID id) {
        return invoiceRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Fatura bulunamadı."));
    }

    private void apply(AuthPrincipal principal, Invoice invoice, UpsertRequest request) {
        invoice.setDirection(request.direction());
        invoice.setInvoiceNumber(blankToNull(request.invoiceNumber()));
        invoice.setIssueDate(request.issueDate());
        invoice.setDueDate(request.dueDate());
        invoice.setPaidDate(request.paidDate());
        invoice.setCurrency(request.currency() == null || request.currency().isBlank() ? "TRY" : request.currency());
        invoice.setPaymentMethod(blankToNull(request.paymentMethod()));
        invoice.setNotes(blankToNull(request.notes()));
        invoice.setVendor(request.vendorId() == null ? null : vendorService.require(principal, request.vendorId()));
        invoice.setCategory(request.categoryId() == null ? null : categoryService.require(principal, request.categoryId()));

        List<InvoiceLine> lines = new ArrayList<>();
        if (request.lines() != null) {
            int order = 0;
            for (LineRequest lineRequest : request.lines()) {
                if (lineRequest.description() == null || lineRequest.description().isBlank()) {
                    continue;
                }
                InvoiceLine line = new InvoiceLine();
                line.setDescription(lineRequest.description().trim());
                line.setQuantity(defaultAmount(lineRequest.quantity(), BigDecimal.ONE));
                line.setUnitPrice(defaultAmount(lineRequest.unitPrice(), BigDecimal.ZERO));
                line.setVatRate(defaultAmount(lineRequest.vatRate(), BigDecimal.ZERO));
                BigDecimal net = line.getQuantity().multiply(line.getUnitPrice());
                BigDecimal vat = net.multiply(line.getVatRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                line.setLineTotal(net.add(vat).setScale(2, RoundingMode.HALF_UP));
                line.setSortOrder(order++);
                lines.add(line);
            }
        }
        invoice.replaceLines(lines);
        recalculate(invoice, request);
        invoice.setStatus(resolveStatus(request, invoice));
    }

    private void recalculate(Invoice invoice, UpsertRequest request) {
        if (!invoice.getLines().isEmpty()) {
            BigDecimal subtotal = invoice.getLines().stream()
                    .map(line -> line.getQuantity().multiply(line.getUnitPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal vat = invoice.getLines().stream()
                    .map(line -> line.getLineTotal().subtract(line.getQuantity().multiply(line.getUnitPrice())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            invoice.setSubtotal(subtotal);
            invoice.setVatAmount(vat);
            invoice.setTotal(subtotal.add(vat));
            return;
        }
        invoice.setSubtotal(defaultAmount(request.subtotal(), BigDecimal.ZERO));
        invoice.setVatAmount(defaultAmount(request.vatAmount(), BigDecimal.ZERO));
        invoice.setTotal(defaultAmount(request.total(), invoice.getSubtotal().add(invoice.getVatAmount())));
    }

    private InvoiceStatus resolveStatus(UpsertRequest request, Invoice invoice) {
        if (request.status() != null) {
            return request.status();
        }
        if (invoice.getPaidDate() != null) {
            return InvoiceStatus.PAID;
        }
        if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(LocalDate.now())) {
            return InvoiceStatus.OVERDUE;
        }
        return invoice.getStatus() == null ? InvoiceStatus.PENDING : invoice.getStatus();
    }

    private BigDecimal defaultAmount(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String label(Invoice invoice) {
        return invoice.getInvoiceNumber() == null ? invoice.getId().toString() : invoice.getInvoiceNumber();
    }

    public record FileDownload(String name, String contentType, byte[] bytes) {
    }
}
