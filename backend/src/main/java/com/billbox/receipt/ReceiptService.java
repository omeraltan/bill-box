package com.billbox.receipt;

import com.billbox.alert.AlertService;
import com.billbox.audit.AuditService;
import com.billbox.category.CategoryService;
import com.billbox.common.exception.ApiException;
import com.billbox.common.web.PageResponse;
import com.billbox.file.FileStorageService;
import com.billbox.organization.OrganizationRepository;
import com.billbox.receipt.ReceiptDtos.DetailResponse;
import com.billbox.receipt.ReceiptDtos.FileResponse;
import com.billbox.receipt.ReceiptDtos.ItemRequest;
import com.billbox.receipt.ReceiptDtos.ItemResponse;
import com.billbox.receipt.ReceiptDtos.SummaryResponse;
import com.billbox.receipt.ReceiptDtos.UpsertRequest;
import com.billbox.security.AuthPrincipal;
import com.billbox.user.UserAccountRepository;
import com.billbox.vendor.VendorService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userAccountRepository;
    private final VendorService vendorService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final AlertService alertService;

    public ReceiptService(
            ReceiptRepository receiptRepository,
            OrganizationRepository organizationRepository,
            UserAccountRepository userAccountRepository,
            VendorService vendorService,
            CategoryService categoryService,
            FileStorageService fileStorageService,
            AuditService auditService,
            AlertService alertService
    ) {
        this.receiptRepository = receiptRepository;
        this.organizationRepository = organizationRepository;
        this.userAccountRepository = userAccountRepository;
        this.vendorService = vendorService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SummaryResponse> search(AuthPrincipal principal, String query, int page, int size) {
        String term = query == null ? "" : query.trim();
        return PageResponse.from(
                receiptRepository.findByOrganizationIdAndMerchantNameContainingIgnoreCase(
                        principal.organizationId(),
                        term,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "purchasedOn"))
                ).map(this::toSummary)
        );
    }

    @Transactional(readOnly = true)
    public DetailResponse get(AuthPrincipal principal, UUID id) {
        return toDetail(require(principal, id));
    }

    @Transactional
    public DetailResponse create(AuthPrincipal principal, UpsertRequest request) {
        Receipt receipt = new Receipt();
        receipt.setOrganization(organizationRepository.getReferenceById(principal.organizationId()));
        receipt.setCreatedBy(userAccountRepository.getReferenceById(principal.userId()));
        apply(principal, receipt, request);
        receiptRepository.save(receipt);
        alertService.evaluateReceipt(receipt);
        auditService.record(principal, "CREATE", "RECEIPT", receipt.getId(), receipt.getMerchantName());
        return toDetail(receipt);
    }

    @Transactional
    public DetailResponse update(AuthPrincipal principal, UUID id, UpsertRequest request) {
        Receipt receipt = require(principal, id);
        apply(principal, receipt, request);
        alertService.evaluateReceipt(receipt);
        auditService.record(principal, "UPDATE", "RECEIPT", receipt.getId(), receipt.getMerchantName());
        return toDetail(receipt);
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID id) {
        Receipt receipt = require(principal, id);
        receipt.getFiles().forEach(file -> {
            try {
                fileStorageService.delete(file.getStorageKey());
            } catch (IOException ignored) {
                // dosya silinemese de kayıt kalkar
            }
        });
        receiptRepository.delete(receipt);
        auditService.record(principal, "DELETE", "RECEIPT", id, receipt.getMerchantName());
    }

    @Transactional
    public DetailResponse attach(AuthPrincipal principal, UUID id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Dosya seçilmedi.");
        }
        Receipt receipt = require(principal, id);
        try {
            String key = fileStorageService.store(principal.organizationId(), "receipts", receipt.getId(), file);
            ReceiptFile stored = new ReceiptFile();
            stored.setReceipt(receipt);
            stored.setOriginalName(file.getOriginalFilename() == null ? "fis" : file.getOriginalFilename());
            stored.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            stored.setStorageKey(key);
            stored.setSizeBytes(file.getSize());
            receipt.getFiles().add(stored);
        } catch (IOException ex) {
            throw ApiException.badRequest("Dosya kaydedilemedi.");
        }
        return toDetail(receipt);
    }

    @Transactional(readOnly = true)
    public StoredFile download(AuthPrincipal principal, UUID receiptId, UUID fileId) {
        Receipt receipt = require(principal, receiptId);
        ReceiptFile file = receipt.getFiles().stream()
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

    public Receipt require(AuthPrincipal principal, UUID id) {
        return receiptRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Fiş bulunamadı."));
    }

    private void apply(AuthPrincipal principal, Receipt receipt, UpsertRequest request) {
        receipt.setMerchantName(request.merchantName().trim());
        receipt.setPurchasedOn(request.purchasedOn());
        receipt.setCurrency(request.currency() == null || request.currency().isBlank() ? "TRY" : request.currency());
        receipt.setPaymentMethod(blank(request.paymentMethod()));
        receipt.setReturnUntil(request.returnUntil());
        receipt.setNotes(blank(request.notes()));
        receipt.setVendor(request.vendorId() == null ? null : vendorService.require(principal, request.vendorId()));
        receipt.setCategory(request.categoryId() == null ? null : categoryService.require(principal, request.categoryId()));
        List<ReceiptItem> items = new ArrayList<>();
        if (request.items() != null) {
            int order = 0;
            for (ItemRequest itemRequest : request.items()) {
                if (itemRequest.description() == null || itemRequest.description().isBlank()) {
                    continue;
                }
                ReceiptItem item = new ReceiptItem();
                item.setDescription(itemRequest.description().trim());
                item.setQuantity(itemRequest.quantity() == null ? BigDecimal.ONE : itemRequest.quantity());
                item.setUnitPrice(itemRequest.unitPrice() == null ? BigDecimal.ZERO : itemRequest.unitPrice());
                item.setLineTotal(item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
                item.setWarrantyMonths(itemRequest.warrantyMonths());
                item.setSortOrder(order++);
                items.add(item);
            }
        }
        receipt.replaceItems(items);
        if (!items.isEmpty()) {
            receipt.setTotal(items.stream().map(ReceiptItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        } else {
            receipt.setTotal(request.total() == null ? BigDecimal.ZERO : request.total());
        }
    }

    private SummaryResponse toSummary(Receipt receipt) {
        return new SummaryResponse(
                receipt.getId(),
                receipt.getMerchantName(),
                receipt.getPurchasedOn(),
                receipt.getReturnUntil(),
                receipt.getCurrency(),
                receipt.getTotal(),
                receipt.getCategory() == null ? null : receipt.getCategory().getName(),
                receipt.getCategory() == null ? null : receipt.getCategory().getColor(),
                receipt.getItems().size(),
                receipt.getFiles().size()
        );
    }

    private DetailResponse toDetail(Receipt receipt) {
        return new DetailResponse(
                receipt.getId(),
                receipt.getVendor() == null ? null : receipt.getVendor().getId(),
                receipt.getVendor() == null ? null : receipt.getVendor().getName(),
                receipt.getCategory() == null ? null : receipt.getCategory().getId(),
                receipt.getCategory() == null ? null : receipt.getCategory().getName(),
                receipt.getMerchantName(),
                receipt.getPurchasedOn(),
                receipt.getCurrency(),
                receipt.getTotal(),
                receipt.getPaymentMethod(),
                receipt.getReturnUntil(),
                receipt.getNotes(),
                receipt.getItems().stream().map(item -> new ItemResponse(
                        item.getId(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal(),
                        item.getWarrantyMonths()
                )).toList(),
                receipt.getFiles().stream().map(file -> new FileResponse(
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
