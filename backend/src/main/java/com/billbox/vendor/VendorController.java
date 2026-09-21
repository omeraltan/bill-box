package com.billbox.vendor;

import com.billbox.common.web.PageResponse;
import com.billbox.security.SecurityUtils;
import com.billbox.vendor.VendorDtos.Response;
import com.billbox.vendor.VendorDtos.UpsertRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public PageResponse<Response> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return vendorService.search(SecurityUtils.current(), q, page, size);
    }

    @GetMapping("/all")
    public List<Response> all() {
        return vendorService.listAll(SecurityUtils.current());
    }

    @PostMapping
    public Response create(@Valid @RequestBody UpsertRequest request) {
        return vendorService.create(SecurityUtils.current(), request);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable UUID id, @Valid @RequestBody UpsertRequest request) {
        return vendorService.update(SecurityUtils.current(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        vendorService.delete(SecurityUtils.current(), id);
    }
}
