package com.billbox.ocr;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InvoiceTextParser {

    private static final List<DateTimeFormatter> DATES = List.of(
            DateTimeFormatter.ofPattern("d.M.uuuu"),
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private static final Pattern INVOICE_NO = Pattern.compile(
            "(?i)(?:fatura\\s*(?:no|numaras[ıi])|invoice\\s*(?:no|number)|ettn)\\s*[:.]?\\s*([A-Z0-9\\-/]{3,40})"
    );
    private static final Pattern TAX = Pattern.compile("(?i)(?:vkn|tckn|vergi\\s*no)\\s*[:.]?\\s*(\\d{10,11})");
    private static final Pattern IBAN = Pattern.compile("(?i)(TR\\d{24})");
    private static final Pattern ISSUE = Pattern.compile("(?i)(?:fatura\\s*tarihi|düzenleme\\s*tarihi|issue\\s*date)\\s*[:.]?\\s*(\\d{1,2}[./-]\\d{1,2}[./-]\\d{4}|\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern DUE = Pattern.compile("(?i)(?:vade|son\\s*ödeme|due\\s*date)\\s*[:.]?\\s*(\\d{1,2}[./-]\\d{1,2}[./-]\\d{4}|\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern TOTAL = Pattern.compile("(?i)(?:ödenecek\\s*tutar|genel\\s*toplam|toplam\\s*tutar|payable\\s*amount)\\s*[:.]?\\s*([0-9.]{1,18},\\d{2}|[0-9,]{1,18}\\.\\d{2})");
    private static final Pattern VAT = Pattern.compile("(?i)(?:kdv\\s*tutarı|hesaplanan\\s*kdv|vat)\\s*[:.]?\\s*([0-9.]{1,18},\\d{2}|[0-9,]{1,18}\\.\\d{2})");
    private static final Pattern SUBTOTAL = Pattern.compile("(?i)(?:mal\\s*hizmet\\s*toplam|ara\\s*toplam|kdv\\s*hariç|line\\s*extension)\\s*[:.]?\\s*([0-9.]{1,18},\\d{2}|[0-9,]{1,18}\\.\\d{2})");
    private static final Pattern VENDOR = Pattern.compile("(?i)(?:satıcı|ünvan|ünvanı|supplier)\\s*[:.]?\\s*(.+)");

    public OcrResult parse(String rawText) {
        List<String> warnings = new ArrayList<>();
        if (rawText == null || rawText.isBlank()) {
            warnings.add("Belgeden metin çıkarılamadı. Alanları elle girebilirsiniz.");
            return new OcrResult(null, null, null, null, null, null, "TRY", null, null, null, rawText, warnings);
        }
        String text = rawText.replace('\u00a0', ' ');
        String invoiceNumber = first(INVOICE_NO, text);
        String taxNumber = first(TAX, text);
        String iban = first(IBAN, text);
        LocalDate issueDate = parseDate(first(ISSUE, text));
        LocalDate dueDate = parseDate(first(DUE, text));
        BigDecimal total = parseMoney(first(TOTAL, text));
        BigDecimal vat = parseMoney(first(VAT, text));
        BigDecimal subtotal = parseMoney(first(SUBTOTAL, text));
        String vendor = first(VENDOR, text);
        if (vendor != null) {
            vendor = vendor.replaceAll("\\s+", " ").trim();
            if (vendor.length() > 200) {
                vendor = vendor.substring(0, 200);
            }
        }
        if (invoiceNumber == null) {
            warnings.add("Fatura numarası otomatik okunamadı.");
        }
        if (total == null) {
            warnings.add("Toplam tutar otomatik okunamadı.");
        }
        return new OcrResult(vendor, taxNumber, invoiceNumber, issueDate, dueDate, iban, "TRY", subtotal, vat, total, text, warnings);
    }

    private String first(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace('-', '.');
        for (DateTimeFormatter formatter : DATES) {
            try {
                return LocalDate.parse(value.contains("-") && value.indexOf('-') == 4 ? value : normalized, formatter);
            } catch (DateTimeParseException ignored) {
                // sonraki format
            }
        }
        return null;
    }

    private BigDecimal parseMoney(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.contains(",") && normalized.lastIndexOf(',') > normalized.lastIndexOf('.')) {
            normalized = normalized.replace(".", "").replace(',', '.');
        } else {
            normalized = normalized.replace(",", "");
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
