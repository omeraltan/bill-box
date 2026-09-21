package com.billbox.vendor;

import com.billbox.audit.AuditService;
import com.billbox.common.exception.ApiException;
import com.billbox.common.web.PageResponse;
import com.billbox.organization.OrganizationRepository;
import com.billbox.security.AuthPrincipal;
import com.billbox.vendor.VendorDtos.Response;
import com.billbox.vendor.VendorDtos.UpsertRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;

    public VendorService(
            VendorRepository vendorRepository,
            OrganizationRepository organizationRepository,
            AuditService auditService
    ) {
        this.vendorRepository = vendorRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<Response> search(AuthPrincipal principal, String query, int page, int size) {
        String term = query == null ? "" : query.trim();
        return PageResponse.from(
                vendorRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                        principal.organizationId(),
                        term,
                        PageRequest.of(page, size)
                ).map(Response::from)
        );
    }

    @Transactional(readOnly = true)
    public List<Response> listAll(AuthPrincipal principal) {
        return vendorRepository.findByOrganizationIdOrderByNameAsc(principal.organizationId())
                .stream()
                .map(Response::from)
                .toList();
    }

    @Transactional
    public Response create(AuthPrincipal principal, UpsertRequest request) {
        Vendor vendor = new Vendor();
        vendor.setOrganization(organizationRepository.getReferenceById(principal.organizationId()));
        apply(vendor, request);
        vendorRepository.save(vendor);
        auditService.record(principal, "CREATE", "VENDOR", vendor.getId(), vendor.getName());
        return Response.from(vendor);
    }

    @Transactional
    public Response update(AuthPrincipal principal, UUID id, UpsertRequest request) {
        Vendor vendor = require(principal, id);
        apply(vendor, request);
        auditService.record(principal, "UPDATE", "VENDOR", vendor.getId(), vendor.getName());
        return Response.from(vendor);
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID id) {
        Vendor vendor = require(principal, id);
        vendorRepository.delete(vendor);
        auditService.record(principal, "DELETE", "VENDOR", id, vendor.getName());
    }

    public Vendor require(AuthPrincipal principal, UUID id) {
        return vendorRepository.findByIdAndOrganizationId(id, principal.organizationId())
                .orElseThrow(() -> ApiException.notFound("Cari bulunamadı."));
    }

    private void apply(Vendor vendor, UpsertRequest request) {
        vendor.setName(request.name().trim());
        vendor.setTaxNumber(blankToNull(request.taxNumber()));
        vendor.setEmail(blankToNull(request.email()));
        vendor.setPhone(blankToNull(request.phone()));
        vendor.setIban(blankToNull(request.iban()));
        vendor.setAddress(blankToNull(request.address()));
        vendor.setPartyType(request.partyType());
        vendor.setNotes(blankToNull(request.notes()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
