package com.billbox.warranty;

import com.billbox.alert.AlertService;
import com.billbox.audit.AuditService;
import com.billbox.common.exception.ApiException;
import com.billbox.common.web.PageResponse;
import com.billbox.file.FileStorageService;
import com.billbox.organization.OrganizationRepository;
import com.billbox.receipt.Receipt;
import com.billbox.receipt.ReceiptItem;
import com.billbox.receipt.ReceiptService;
import com.billbox.security.AuthPrincipal;
import com.billbox.user.UserAccountRepository;
import com.billbox.vendor.VendorService;
import com.billbox.warranty.WarrantyDtos.DetailResponse;
import com.billbox.warranty.WarrantyDtos.FileResponse;
import com.billbox.warranty.WarrantyDtos.SummaryResponse;
import com.billbox.warranty.WarrantyDtos.UpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userAccountRepository;
    private final VendorService vendorService;
    private final ReceiptService receiptService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final AlertService alertService;

    public WarrantyService(
            WarrantyRepository warrantyRepository,
            OrganizationRepository organizationRepository,
            UserAccountRepository userAccountRepository,
            VendorService vendorService,
            ReceiptService receiptService,
            FileStorageService fileStorageService,
            AuditService auditService,
            AlertService alertService
    ) {
        this.warrantyRepository = warrantyRepository;
        this.organizationRepository = organizationRepository;
        this.userAccountRepository = userAccountRepository;
        this.vendorService = vendorService;
        this.receiptService = receiptService;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SummaryResponse> search(AuthPrincipal principal, CoverageStatus status, int page, int size) {
        if (status != null) {
            List<SummaryResponse> filtered = warrantyRepository
                    .findByOrganizationIdOrderByWarrantyEndsOnAsc(principal.organizationId())
                    .stream()
                    .map(this::toSummary)
                    .filter(item -> item.status() == status)
                    .toList();
            int from = Math.min(page * size, filtered.size());
            int to = Math.min(from + size, filtered.size());
            return PageResponse.from(new PageImpl<>(filtered.subList(from, to), PageRequest.of(page, size), filtered.size()));
        }
        Page<Warranty> result = warrantyRepository.findByOrganizationId(
                principal.organizationId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "warrantyEndsOn"))
        );
        return PageResponse.from(result.map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public DetailResponse get(AuthPrincipal principal, UUID id) {
        return toDetail(require(principal, id));
    }

    @Transactional
    public DetailResponse create(AuthPrincipal principal, UpsertRequest request) {
        Warranty warranty = new Warranty();
        warranty.setOrganization(organizationRepository.getReferenceById(principal.organizationId()));
        warranty.setCreatedBy(userAccountRepository.getReferenceById(principal.userId()));
        apply(principal, warranty, request);
        warrantyRepository.save(warranty);
        alertService.evaluateWarranty(warranty);
        auditService.record(principal, "CREATE", "WARRANTY", warranty.getId(), warranty.getProductName());
        return toDetail(warranty);
    }

    @Transactional
    public DetailResponse createFromReceiptItem(AuthPrincipal principal, UUID receiptId, UUID itemId, int months) {
        if (months < 1 || months > 120) {
            throw ApiException.badRequest("Garanti süresi 1 ile 120 ay arasında olmalı.");
        }
        Receipt receipt = receiptService.require(principal, receiptId);
        ReceiptItem item = receipt.getItems().stream()
                .filter(candidate -> candidate.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Fiş kalemi bulunamadı."));
        UpsertRequest request = new UpsertRequest(
                receipt.getId(),
                receipt.getVendor() == null ? null : receipt.getVendor().getId(),
                item.getDescription(),
                null,
                null,
                receipt.getMerchantName(),
                receipt.getPurchasedOn(),
                receipt.getPurchasedOn().plusMonths(months),
                receipt.getReturnUntil(),
                "Fiş kaleminden oluşturuldu"
        );
        return create(principal, request);
    }

    @Transactional
    public DetailResponse update(AuthPrincipal principal, UUID id, UpsertRequest request) {
        Warranty warranty = require(principal, id);
        apply(principal, warranty, request);
        alertService.evaluateWarranty(warranty);
        auditService.record(principal, "UPDATE", "WARRANTY", warranty.getId(), warranty.getProductName());
        return toDetail(warranty);
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID id) {
        Warranty warranty = require(principal, id);
        warranty.getFiles().forEach(file -> {
            try {
                fileStorageService.delete(file.getStorageKey());
            } catch (IOException ignored) {
                // dosya silinemese de kayıt kalkar
            }
        });
        warrantyRepository.delete(warranty);
        auditService.record(principal, "DELETE", "WARRANTY", id, warranty.getProductName());
    }

    @Transactional
    public DetailResponse attach(AuthPrincipal principal, UUID id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Dosya seçilmedi.");
        }
        Warranty warranty = require(principal, id);
        try {
            String key = fileStorageService.store(principal.organizationId(), "warranties", warranty.getId(), file);
            WarrantyFile stored = new WarrantyFile();
            stored.setWarranty(warranty);
            stored.setOriginalName(file.getOriginalFilename() == null ? "garanti" : file.getOriginalFilename());
            stored.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            stored.setStorageKey(key);
            stored.setSizeBytes(file.getSize());
            warranty.getFiles().add(stored);
        } catch (IOException ex) {
            throw ApiException.badRequest("Dosya kaydedilemedi.");
        }
        return toDetail(warranty);
    }

    @Transactional(readOnly = true)
    public StoredFile download(AuthPrincipal principal, UUID warrantyId, UUID fileId) {
        Warranty warranty = require(principal, warrantyId);
        WarrantyFile file = warranty.getFiles().stream()
                .filter(item -> item.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Dosya bulunamadı."));
        try {
            byte[] bytes = Files.readAllBytes(fileStorageService.resolve(file.getStorageKey()));
            return new StoredFile(file.getOriginalName(), file.getContentType(), bytes);
        } catch (IOException ex) {
            throw ApiException.notFound("Dosya okunamadı.");
        }
    }

    public Warranty require(AuthPrincipal principal, UUID id) {
        return warrantyRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Garanti belgesi bulunamadı."));
    }

    private void apply(AuthPrincipal principal, Warranty warranty, UpsertRequest request) {
        if (request.warrantyEndsOn().isBefore(request.purchasedOn())) {
            throw ApiException.badRequest("Garanti bitişi alış tarihinden önce olamaz.");
        }
        warranty.setProductName(request.productName().trim());
        warranty.setBrand(blank(request.brand()));
        warranty.setSerialNumber(blank(request.serialNumber()));
        warranty.setMerchantName(blank(request.merchantName()));
        warranty.setPurchasedOn(request.purchasedOn());
        warranty.setWarrantyEndsOn(request.warrantyEndsOn());
        warranty.setReturnUntil(request.returnUntil());
        warranty.setNotes(blank(request.notes()));
        warranty.setVendor(request.vendorId() == null ? null : vendorService.require(principal, request.vendorId()));
        warranty.setReceipt(request.receiptId() == null ? null : receiptService.require(principal, request.receiptId()));
    }

    private SummaryResponse toSummary(Warranty warranty) {
        LocalDate today = LocalDate.now();
        return new SummaryResponse(
                warranty.getId(),
                warranty.getProductName(),
                warranty.getBrand(),
                warranty.getMerchantName(),
                warranty.getPurchasedOn(),
                warranty.getWarrantyEndsOn(),
                warranty.getReturnUntil(),
                CoverageStatus.of(warranty.getWarrantyEndsOn(), today),
                CoverageStatus.daysRemaining(warranty.getWarrantyEndsOn(), today),
                warranty.getReceipt() == null ? null : warranty.getReceipt().getId()
        );
    }

    private DetailResponse toDetail(Warranty warranty) {
        LocalDate today = LocalDate.now();
        return new DetailResponse(
                warranty.getId(),
                warranty.getReceipt() == null ? null : warranty.getReceipt().getId(),
                warranty.getVendor() == null ? null : warranty.getVendor().getId(),
                warranty.getProductName(),
                warranty.getBrand(),
                warranty.getSerialNumber(),
                warranty.getMerchantName(),
                warranty.getPurchasedOn(),
                warranty.getWarrantyEndsOn(),
                warranty.getReturnUntil(),
                warranty.getNotes(),
                CoverageStatus.of(warranty.getWarrantyEndsOn(), today),
                CoverageStatus.daysRemaining(warranty.getWarrantyEndsOn(), today),
                warranty.getFiles().stream().map(file -> new FileResponse(
                        file.getId(),
                        file.getOriginalName(),
                        file.getContentType(),
                        file.getSizeBytes(),
                        file.getCreatedAt()
                )).toList()
        );
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record StoredFile(String name, String contentType, byte[] bytes) {
    }
}
