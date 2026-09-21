package com.billbox.ubl;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.common.exception.ApiException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class UblInvoiceParser {

    public ParsedUbl parse(String xml, InvoiceDirection fallbackDirection) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            Element root = document.getDocumentElement();

            String invoiceNumber = text(root, "ID");
            LocalDate issueDate = date(text(root, "IssueDate"));
            LocalDate dueDate = date(first(root, "DueDate", "PaymentDueDate"));
            String currency = text(root, "DocumentCurrencyCode");
            if (currency == null || currency.isBlank()) {
                currency = "TRY";
            }

            Element supplier = firstElement(root, "AccountingSupplierParty");
            Element customer = firstElement(root, "AccountingCustomerParty");
            Element party = fallbackDirection == InvoiceDirection.INCOME ? customer : supplier;
            String vendorName = party == null ? null : first(party, "Name", "RegistrationName");
            String taxNumber = party == null ? null : firstId(party);
            String iban = first(root, "PaymentID", "ID");

            Element totals = firstElement(root, "LegalMonetaryTotal");
            BigDecimal subtotal = money(totals, "LineExtensionAmount", "TaxExclusiveAmount");
            BigDecimal total = money(totals, "PayableAmount", "TaxInclusiveAmount");
            BigDecimal vat = money(firstElement(root, "TaxTotal"), "TaxAmount");

            List<ParsedUbl.Line> lines = new ArrayList<>();
            NodeList invoiceLines = root.getElementsByTagNameNS("*", "InvoiceLine");
            for (int i = 0; i < invoiceLines.getLength(); i++) {
                Element line = (Element) invoiceLines.item(i);
                String description = first(line, "Name", "Description");
                BigDecimal quantity = decimal(text(line, "InvoicedQuantity"), BigDecimal.ONE);
                BigDecimal unitPrice = money(firstElement(line, "Price"), "PriceAmount");
                BigDecimal vatRate = money(firstElement(line, "ClassifiedTaxCategory"), "Percent");
                lines.add(new ParsedUbl.Line(
                        description == null ? "Kalem" : description,
                        quantity,
                        unitPrice == null ? BigDecimal.ZERO : unitPrice,
                        vatRate == null ? BigDecimal.ZERO : vatRate
                ));
            }

            return new ParsedUbl(
                    invoiceNumber,
                    issueDate == null ? LocalDate.now() : issueDate,
                    dueDate,
                    currency,
                    vendorName,
                    taxNumber,
                    iban,
                    subtotal == null ? BigDecimal.ZERO : subtotal,
                    vat == null ? BigDecimal.ZERO : vat,
                    total == null ? BigDecimal.ZERO : total,
                    lines,
                    xml
            );
        } catch (Exception ex) {
            throw ApiException.badRequest("UBL-TR faturası okunamadı. XML dosyasını kontrol edin.");
        }
    }

    private String firstId(Element party) {
        NodeList ids = party.getElementsByTagNameNS("*", "ID");
        for (int i = 0; i < ids.getLength(); i++) {
            String value = ids.item(i).getTextContent();
            if (value != null && value.matches("\\d{10,11}")) {
                return value.trim();
            }
        }
        return null;
    }

    private String first(Element element, String... localNames) {
        for (String name : localNames) {
            String value = text(element, name);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String text(Element element, String localName) {
        if (element == null) {
            return null;
        }
        NodeList nodes = element.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        String value = nodes.item(0).getTextContent();
        return value == null ? null : value.trim();
    }

    private Element firstElement(Element element, String localName) {
        if (element == null) {
            return null;
        }
        NodeList nodes = element.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private LocalDate date(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private BigDecimal money(Element element, String... names) {
        String value = first(element, names);
        return value == null ? null : new BigDecimal(value);
    }

    private BigDecimal decimal(String value, BigDecimal fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return new BigDecimal(value);
    }

    public record ParsedUbl(
            String invoiceNumber,
            LocalDate issueDate,
            LocalDate dueDate,
            String currency,
            String vendorName,
            String taxNumber,
            String iban,
            BigDecimal subtotal,
            BigDecimal vatAmount,
            BigDecimal total,
            List<Line> lines,
            String xml
    ) {
        public record Line(String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal vatRate) {
        }
    }
}
