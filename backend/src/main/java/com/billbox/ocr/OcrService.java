package com.billbox.ocr;

import com.billbox.common.exception.ApiException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class OcrService {

    private final InvoiceTextParser invoiceTextParser;

    public OcrService(InvoiceTextParser invoiceTextParser) {
        this.invoiceTextParser = invoiceTextParser;
    }

    public OcrResult extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Okunacak dosya seçilmedi.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try {
            if (contentType.contains("pdf") || name.endsWith(".pdf")) {
                return invoiceTextParser.parse(extractPdf(file.getBytes()));
            }
            if (contentType.contains("xml") || name.endsWith(".xml")) {
                return invoiceTextParser.parse(new String(file.getBytes(), StandardCharsets.UTF_8));
            }
            return new OcrResult(
                    null, null, null, null, null, null, "TRY", null, null, null, null,
                    List.of("Görsel belgeler için metin katmanı yok. PDF veya UBL XML yükleyin; alanları elle tamamlayabilirsiniz.")
            );
        } catch (IOException ex) {
            throw ApiException.badRequest("Belge okunamadı.");
        }
    }

    private String extractPdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
