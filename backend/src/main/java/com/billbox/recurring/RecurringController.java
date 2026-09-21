package com.billbox.recurring;

import com.billbox.recurring.RecurringDtos.Response;
import com.billbox.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recurring")
public class RecurringController {

    private final RecurringService recurringService;

    public RecurringController(RecurringService recurringService) {
        this.recurringService = recurringService;
    }

    @GetMapping
    public List<Response> list() {
        return recurringService.list(SecurityUtils.current());
    }

    @PostMapping
    public Response create(@Valid @RequestBody RecurringDtos.UpsertRequest request) {
        return recurringService.create(SecurityUtils.current(), request);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable UUID id, @Valid @RequestBody RecurringDtos.UpsertRequest request) {
        return recurringService.update(SecurityUtils.current(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        recurringService.delete(SecurityUtils.current(), id);
    }
}
