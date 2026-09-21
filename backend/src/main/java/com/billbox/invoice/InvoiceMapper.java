package com.billbox.invoice;

import com.billbox.invoice.InvoiceDtos.DetailResponse;
import com.billbox.invoice.InvoiceDtos.FileResponse;
import com.billbox.invoice.InvoiceDtos.LineResponse;
import com.billbox.invoice.InvoiceDtos.SummaryResponse;

import java.util.List;

public final class InvoiceMapper {

    private InvoiceMapper() {
    }

    public static SummaryResponse toSummary(Invoice invoice) {
        return new SummaryResponse(
                invoice.getId(),
                invoice.getDirection(),
                invoice.getStatus(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getPaidDate(),
                invoice.getCurrency(),
                invoice.getTotal(),
                invoice.getVendor() == null ? null : invoice.getVendor().getName(),
                invoice.getCategory() == null ? null : invoice.getCategory().getName(),
                invoice.getCategory() == null ? null : invoice.getCategory().getColor()
        );
    }

    public static DetailResponse toDetail(Invoice invoice) {
        List<LineResponse> lines = invoice.getLines().stream()
                .map(line -> new LineResponse(
                        line.getId(),
                        line.getDescription(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getVatRate(),
                        line.getLineTotal(),
                        line.getSortOrder()
                ))
                .toList();
        List<FileResponse> files = invoice.getFiles().stream()
                .map(file -> new FileResponse(
                        file.getId(),
                        file.getOriginalName(),
                        file.getContentType(),
                        file.getSizeBytes(),
                        file.getCreatedAt()
                ))
                .toList();
        return new DetailResponse(
                invoice.getId(),
                invoice.getDirection(),
                invoice.getStatus(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getPaidDate(),
                invoice.getCurrency(),
                invoice.getSubtotal(),
                invoice.getVatAmount(),
                invoice.getTotal(),
                invoice.getPaymentMethod(),
                invoice.getNotes(),
                invoice.getVendor() == null ? null : invoice.getVendor().getId(),
                invoice.getVendor() == null ? null : invoice.getVendor().getName(),
                invoice.getCategory() == null ? null : invoice.getCategory().getId(),
                invoice.getCategory() == null ? null : invoice.getCategory().getName(),
                lines,
                files
        );
    }
}
