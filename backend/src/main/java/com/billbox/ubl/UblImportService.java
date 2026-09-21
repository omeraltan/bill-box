package com.billbox.ubl;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.enums.PartyType;
import com.billbox.common.exception.ApiException;
import com.billbox.invoice.Invoice;
import com.billbox.invoice.InvoiceDtos.DetailResponse;
import com.billbox.invoice.InvoiceDtos.LineRequest;
import com.billbox.invoice.InvoiceDtos.UpsertRequest;
import com.billbox.invoice.InvoiceMapper;
import com.billbox.invoice.InvoiceService;
import com.billbox.security.AuthPrincipal;
import com.billbox.ubl.UblInvoiceParser.ParsedUbl;
import com.billbox.vendor.Vendor;
import com.billbox.vendor.VendorDtos;
import com.billbox.vendor.VendorRepository;
import com.billbox.vendor.VendorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class UblImportService {

    private final UblInvoiceParser ublInvoiceParser;
    private final VendorRepository vendorRepository;
    private final VendorService vendorService;
    private final InvoiceService invoiceService;

    public UblImportService(
            UblInvoiceParser ublInvoiceParser,
            VendorRepository vendorRepository,
            VendorService vendorService,
            InvoiceService invoiceService
    ) {
        this.ublInvoiceParser = ublInvoiceParser;
        this.vendorRepository = vendorRepository;
        this.vendorService = vendorService;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public DetailResponse importXml(AuthPrincipal principal, MultipartFile file, InvoiceDirection direction) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("UBL dosyası seçilmedi.");
        }
        try {
            String xml = new String(file.getBytes(), StandardCharsets.UTF_8);
            ParsedUbl parsed = ublInvoiceParser.parse(xml, direction);
            Vendor vendor = resolveVendor(principal, parsed, direction);
            List<LineRequest> lines = parsed.lines().stream()
                    .map(line -> new LineRequest(line.description(), line.quantity(), line.unitPrice(), line.vatRate()))
                    .toList();
            UpsertRequest request = new UpsertRequest(
                    direction,
                    vendor.getId(),
                    null,
                    parsed.invoiceNumber(),
                    parsed.issueDate(),
                    parsed.dueDate(),
                    null,
                    parsed.currency(),
                    null,
                    null,
                    "UBL-TR içe aktarma",
                    parsed.subtotal(),
                    parsed.vatAmount(),
                    parsed.total(),
                    lines
            );
            DetailResponse created = invoiceService.create(principal, request);
            Invoice invoice = invoiceService.require(principal, created.id());
            invoice.setUblXml(xml);
            return InvoiceMapper.toDetail(invoice);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.badRequest("UBL dosyası işlenemedi.");
        }
    }

    private Vendor resolveVendor(AuthPrincipal principal, ParsedUbl parsed, InvoiceDirection direction) {
        if (parsed.taxNumber() != null) {
            var existing = vendorRepository.findFirstByOrganizationIdAndTaxNumber(
                    principal.organizationId(), parsed.taxNumber());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        if (parsed.vendorName() != null) {
            var existing = vendorRepository.findFirstByOrganizationIdAndNameIgnoreCase(
                    principal.organizationId(), parsed.vendorName());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return vendorService.require(principal, vendorService.create(principal, new VendorDtos.UpsertRequest(
                parsed.vendorName() == null ? "UBL Cari" : parsed.vendorName(),
                parsed.taxNumber(),
                null,
                null,
                parsed.iban(),
                null,
                direction == InvoiceDirection.INCOME ? PartyType.CUSTOMER : PartyType.SUPPLIER,
                "UBL-TR içe aktarma ile oluşturuldu"
        )).id());
    }
}
