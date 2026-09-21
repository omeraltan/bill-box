package com.billbox.ubl;

import com.billbox.common.enums.InvoiceDirection;
import com.billbox.invoice.InvoiceDtos.DetailResponse;
import com.billbox.security.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ubl")
public class UblImportController {

    private final UblImportService ublImportService;

    public UblImportController(UblImportService ublImportService) {
        this.ublImportService = ublImportService;
    }

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DetailResponse importXml(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "EXPENSE") InvoiceDirection direction
    ) {
        return ublImportService.importXml(SecurityUtils.current(), file, direction);
    }
}
